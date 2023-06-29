package org.jmotor.sbt.artifact.metadata.loader

import com.google.common.base.Charsets
import org.apache.ivy.core.IvyPatternHelper
import org.apache.maven.artifact.versioning.{ArtifactVersion, DefaultArtifactVersion}
import org.jmotor.sbt.artifact.exception.ArtifactNotFoundException
import org.jmotor.sbt.artifact.metadata.MetadataLoader

import java.nio.file.Files
import scala.collection.JavaConverters.*
import scala.concurrent.{ExecutionContext, Future}

/**
 * Component: Description: Date: 2018/2/8
 *
 * @author
 *   AI
 */
class IvyPatternsMetadataLoader(patterns: Seq[String])(implicit ec: ExecutionContext) extends MetadataLoader {

  private[this] lazy val regex = """<a(?:onclick="navi\(event\)")? href=":?([^/]*)/"(?: rel="nofollow")?>\1/</a>""".r

  override def getVersions(
    organization: String,
    artifactId: String,
    attrs: Map[String, String]
  ): Future[Seq[ArtifactVersion]] = {
    val urls = patterns.map(pattern => getRevisionUrl(pattern, organization, artifactId, attrs)).collect {
      case Some(url) => url
    }
    val futures = urls.map { url =>
      download(organization, artifactId, url)
    }
    Future.sequence(futures).map { responses =>
      val versions = responses.flatMap { file =>
        regex.findAllMatchIn(Files.readString(file, Charsets.UTF_8)).map(_.group(1)).map { version =>
          new DefaultArtifactVersion(version)
        }
      }
      if (versions.isEmpty) {
        throw ArtifactNotFoundException(organization, artifactId)
      }
      versions
    }
  }

  private def getRevisionUrl(
    pattern: String,
    organization: String,
    artifactId: String,
    attrs: Map[String, String]
  ): Option[String] = {
    val tokens = attrs +
      (IvyPatternHelper.MODULE_KEY        -> artifactId) +
      (IvyPatternHelper.ORGANISATION_KEY  -> organization) +
      (IvyPatternHelper.ORGANISATION_KEY2 -> organization)
    val substituted = IvyPatternHelper.substituteTokens(pattern, tokens.asJava)
    if (IvyPatternHelper.getFirstToken(substituted) == IvyPatternHelper.REVISION_KEY) {
      Some(IvyPatternHelper.getTokenRoot(substituted))
    } else {
      None
    }
  }

}
