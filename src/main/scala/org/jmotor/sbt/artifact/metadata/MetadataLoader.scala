package org.jmotor.sbt.artifact.metadata

import org.apache.ivy.util.url.URLHandlerRegistry
import org.apache.ivy.util.{CopyProgressEvent, CopyProgressListener}
import org.apache.maven.artifact.versioning.ArtifactVersion
import org.jmotor.sbt.artifact.exception.ArtifactNotFoundException

import java.net.{URL, URI}
import java.nio.file.{Files, Path, Paths}
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.io.Source
import java.io.ByteArrayInputStream
import java.net.HttpURLConnection
import java.io.FileOutputStream

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
        val src        = new URI(url).toURL()
        val connection = src.openConnection()

        Option(connection.getInputStream()).map { is =>
          val path = Files.createTempFile(s"maven-metadata-$organization-$artifactId", ".xml")
          val os   = new FileOutputStream(path.toAbsolutePath.toString())

          try {
            val buffer    = new Array[Byte](8192)
            var bytesRead = is.read(buffer)
            while (bytesRead >= 0) {
              os.write(buffer, 0, bytesRead)
              bytesRead = is.read(buffer)
            }
            path
          } finally {
            is.close()
            os.close()
          }
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
