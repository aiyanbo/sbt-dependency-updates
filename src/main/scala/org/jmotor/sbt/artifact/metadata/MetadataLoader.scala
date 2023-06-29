package org.jmotor.sbt.artifact.metadata

import org.apache.ivy.util.url.URLHandlerRegistry
import org.apache.ivy.util.{CopyProgressEvent, CopyProgressListener}
import org.apache.maven.artifact.versioning.ArtifactVersion
import org.jmotor.sbt.artifact.exception.ArtifactNotFoundException

import java.net.URL
import java.nio.file.{Files, Path, Paths}
import scala.concurrent.{ExecutionContext, Future, Promise}

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

  def download(organization: String, artifactId: String, url: String)(implicit ec: ExecutionContext): Future[Path] = {
    val src        = new URL(url)
    val dispatcher = URLHandlerRegistry.getDefault
    Future {
      dispatcher.getURLInfo(src)
    }.flatMap {
      case info if info.isReachable =>
        val promise = Promise[Path]
        val path    = Files.createTempFile(s"maven-metadata-$organization-$artifactId", ".xml")
        try {
          dispatcher.download(
            src,
            path.toFile,
            new CopyProgressListener {
              override def start(evt: CopyProgressEvent): Unit = {}

              override def progress(evt: CopyProgressEvent): Unit = {}

              override def end(evt: CopyProgressEvent): Unit =
                promise.success(path)
            }
          )
        } catch {
          case e: Throwable => promise.failure(e)
        }
        promise.future
      case _ => throw ArtifactNotFoundException(organization, artifactId)
    }
  }
}
