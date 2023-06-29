package org.jmotor.sbt.artifact.maven

import Conversions.*
import com.fasterxml.jackson.core.`type`.TypeReference
import okhttp3.Response
import org.jmotor.sbt.extra.Clients.{OkHttpCallWrapper, jackson, okhttpClient, withResponse}

import scala.concurrent.{ExecutionContext, Future}

class SearchClient() {
  private[this] lazy val root                             = "https://search.maven.org/solrsearch/select"
  private[this] lazy val tr: TypeReference[Seq[Artifact]] = new TypeReference[Seq[Artifact]] {}

  def search(request: SearchRequest)(implicit executor: ExecutionContext): Future[Seq[Artifact]] =
    okhttpClient.newCall(request.toHttpRequest(root).build()).toFuture.map(unpacking)

  def selectAll(groupId: String, artifactId: String)(implicit executor: ExecutionContext): Future[Seq[Artifact]] = {
    val totalRequest = SearchRequest(Some(groupId), Some(artifactId), None, core = "ga")
    okhttpClient.newCall(totalRequest.toHttpRequest(root).build()).toFuture.flatMap { response =>
      withResponse(response) {
        response match {
          case response if response.isSuccessful =>
            val rows = 50
            val count = (for (m <- """"versionCount": ?(\d+),""".r findFirstMatchIn response.body().string())
              yield m group 1).getOrElse("0").toInt
            if (count > 0) {
              Future
                .sequence((0 to pages(count, rows)).map { index =>
                  val request = SearchRequest(Some(groupId), Some(artifactId), None, rows = rows, start = index * rows)
                  okhttpClient.newCall(request.toHttpRequest(root).build()).toFuture
                })
                .map(responses => responses.flatMap(unpacking))
            } else {
              Future.successful(Seq.empty[Artifact])
            }
          case _ => Future.successful(Seq.empty[Artifact])
        }
      }
    }
  }

  def latestVersion(groupId: String, artifactId: String)(implicit
    executor: ExecutionContext
  ): Future[Option[String]] = {
    val request = SearchRequest(Some(groupId), Some(artifactId), None, core = "ga", rows = 1)
    okhttpClient.newCall(request.toHttpRequest(root).build()).toFuture.map { response =>
      withResponse(response) {
        response match {
          case response if response.isSuccessful =>
            for (m <- """"latestVersion": ?"(.*)"""".r findFirstMatchIn response.body().string()) yield m group 1
          case _ => None
        }
      }
    }
  }

  private def pages(count: Int, rows: Int): Int =
    if (count % rows == 0) {
      count / rows
    } else {
      (count / rows) + 1
    }

  private def unpacking(response: Response): Seq[Artifact] =
    withResponse(response) {
      if (response.isSuccessful) {
        val docs = for (m <- """"docs" ?: ?(.*)""".r findFirstMatchIn response.body().string()) yield m group 1
        jackson.readValue[Seq[Artifact]](docs.getOrElse("[]"), tr)
      } else {
        Seq.empty[Artifact]
      }
    }

}

object SearchClient {
  def apply(): SearchClient =
    new SearchClient()

}
