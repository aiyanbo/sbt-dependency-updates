package org.jmotor.sbt.service

import java.util.concurrent.{Executors, TimeUnit}

import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.{CloseableHttpClient, HttpClients}
import org.apache.http.util.EntityUtils
import org.jmotor.sbt.model.{ModuleStatus, Status}
import org.jmotor.sbt.util.ModuleStatusParser
import sbt.ModuleID

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

/**
 * Component:
 * Description:
 * Date: 2016/12/22
 *
 * @author AI
 */
object ModuleUpdatesService {

  private[this] val updatesHost = "https://stack-badges.herokuapp.com"
  private[this] val hc: CloseableHttpClient = HttpClients.custom()
    .setMaxConnTotal(20)
    .setMaxConnPerRoute(20)
    .setConnectionTimeToLive(5, TimeUnit.MINUTES).build()
  private[this] val executionContext = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(10))

  def resolve(modules: Seq[ModuleID]): Seq[ModuleStatus] = {
    val result = Future.traverse(modules)(check)
    Await.result(result, 1.minutes)
  }

  private[this] def check(module: ModuleID): Future[ModuleStatus] = {
    val location = s"$updatesHost/maven-central/resolves/${module.organization}/${module.name}/${module.revision}"
    val get = new HttpGet(location)
    Future {
      hc.execute(get)
    }(executionContext) map { response ⇒
      response.getStatusLine.getStatusCode match {
        case s if s / 100 == 2 ⇒
          val body = EntityUtils.toString(response.getEntity, "utf-8")
          val (status, version) = ModuleStatusParser.parse(body)
          ModuleStatus(module.organization, module.name, module.revision, Status.withName(status), version)
        case 404 ⇒ ModuleStatus(module.organization, module.name, module.revision, Status.NotFound, "")
        case _   ⇒ ModuleStatus(module.organization, module.name, module.revision, Status.Error, "")
      }
    } recover {
      case t: Throwable ⇒ ModuleStatus(module.organization, module.name, module.revision, Status.Error, t.getLocalizedMessage)
    }
  }

}
