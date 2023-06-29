package org.jmotor.sbt.artifact.metadata.loader

import org.apache.maven.artifact.versioning.{ArtifactVersion, DefaultArtifactVersion}
import org.jmotor.sbt.artifact.exception.ArtifactNotFoundException
import org.jmotor.sbt.artifact.maven.{SearchClient, SearchRequest}
import org.jmotor.sbt.artifact.metadata.MetadataLoader

import scala.concurrent.{ExecutionContext, Future}

/**
 * Component: Description: Date: 2018/2/8
 *
 * @author
 *   AI
 */
class MavenSearchMetadataLoader(maxRows: Int, client: SearchClient)(implicit ec: ExecutionContext)
    extends MetadataLoader {

  override def getVersions(
    organization: String,
    artifactId: String,
    attrs: Map[String, String]
  ): Future[Seq[ArtifactVersion]] = {
    val request = SearchRequest(Some(organization), Some(artifactId), None, rows = maxRows)
    client.search(request).map {
      case artifacts if artifacts.isEmpty => throw ArtifactNotFoundException(organization, artifactId)
      case artifacts                      => artifacts.map(a => new DefaultArtifactVersion(a.v))
    }
  }

}

object MavenSearchMetadataLoader {

  def apply(maxRows: Int)(implicit ec: ExecutionContext): MavenSearchMetadataLoader =
    new MavenSearchMetadataLoader(maxRows, SearchClient())

}
