package org.jmotor.sbt.service

import org.asynchttpclient.{AsyncCompletionHandler, DefaultAsyncHttpClient, Response}
import org.jmotor.sbt.model.{ModuleStatus, Status}
import org.jmotor.sbt.util.ModuleStatusParser
import sbt.ModuleID

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future, Promise}

/**
 * Component:
 * Description:
 * Date: 2016/12/22
 *
 * @author AI
 */
object ModuleUpdatesService {

  private[this] val httpClient = new DefaultAsyncHttpClient()
  private[this] val updatesHost = "https://stack-badges.herokuapp.com"

  def resolve(modules: Seq[ModuleID]): Seq[ModuleStatus] = {
    val result = Future.traverse(modules)(check)
    Await.result(result, 1.minutes)
  }

  private[this] def check(module: ModuleID): Future[ModuleStatus] = {
    val result = Promise[ModuleStatus]
    httpClient.prepareGet(s"$updatesHost/maven-central/resolves/${module.organization}/${module.name}/${module.revision}")
      .execute(new AsyncCompletionHandler[ModuleStatus] {
        override def onCompleted(response: Response): ModuleStatus = {
          val status = response.getStatusCode match {
            case s if s / 100 == 2 ⇒
              val (status, version) = ModuleStatusParser.parse(response.getResponseBody)
              ModuleStatus(module.organization, module.name, module.revision, Status.withName(status), version)
            case 404 ⇒ ModuleStatus(module.organization, module.name, module.revision, Status.NotFound, "")
            case _   ⇒ ModuleStatus(module.organization, module.name, module.revision, Status.Error, "")
          }
          result.success(status)
          status
        }

        override def onThrowable(t: Throwable): Unit = result.failure(t)
      })
    result.future
  }

}
