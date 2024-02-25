package org.jmotor.sbt.artifact.metadata

import org.apache.ivy.util.url.URLHandlerRegistry
import org.apache.ivy.util.{CopyProgressEvent, CopyProgressListener}
import org.apache.maven.artifact.versioning.ArtifactVersion
import org.jmotor.sbt.artifact.exception.ArtifactNotFoundException

import java.net.URL
import java.nio.file.{Files, Path, Paths}
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.io.Source
import java.io.ByteArrayInputStream
import java.net.HttpURLConnection

/**
 * Component: Description: Date: 2018/2/8
 *
 * @author
 *   AI
 */
trait MetadataLoader {

  def getVersions(
    organization: String,
    artifactId: String,
    attrs: Map[String, String] = Map.empty
  ): Future[Seq[ArtifactVersion]]

  def download(organization: String, artifactId: String, url: String)(implicit ec: ExecutionContext): Future[Path] =
    Future {
      try {
        val src        = new URL(url)
        val connection = src.openConnection()

        Option(connection.getInputStream()).map { is =>
          val path = Files.createTempFile(s"maven-metadata-$organization-$artifactId", ".xml")
          // might better write via file output stream
          Files.write(path, is.readAllBytes())
          path
        }
      } catch {
        case e: java.io.FileNotFoundException => None
        case e: Throwable                     => throw e
      }
    }.flatMap {
      case None       => Future.failed(ArtifactNotFoundException(organization, artifactId))
      case Some(path) => Future.successful(path)
    }
}
