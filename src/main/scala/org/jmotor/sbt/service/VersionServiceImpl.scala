package org.jmotor.sbt.service

import org.apache.maven.artifact.versioning.{ArtifactVersion, DefaultArtifactVersion}
import org.jmotor.sbt.artifact.Versions
import org.jmotor.sbt.artifact.exception.ArtifactNotFoundException
import org.jmotor.sbt.artifact.metadata.MetadataLoader
import org.jmotor.sbt.artifact.metadata.loader.{
  IvyPatternsMetadataLoader,
  MavenRepoMetadataLoader,
  MavenSearchMetadataLoader
}
import org.jmotor.sbt.dto.{ModuleStatus, Status}
import org.jmotor.sbt.exception.MultiException
import org.jmotor.sbt.metadata.MetadataLoaderGroup
import sbt.librarymanagement.{MavenRepository, ModuleID, Resolver, URLRepository}
import sbt.util.Logger

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NonFatal
import scala.util.{Failure, Success}

/**
 * Component: Description: Date: 2018/2/9
 *
 * @author
 * AI
 */
class VersionServiceImpl(logger: Logger, scalaVersion: String, scalaBinaryVersion: String, resolvers: Seq[Resolver])
    extends VersionService {

  private[this] lazy val groups = getLoaderGroups(resolvers)

  override def checkForUpdates(module: ModuleID): Future[ModuleStatus] = check(module)

  override def checkPluginForUpdates(
    module: ModuleID,
    sbtBinaryVersion: String,
    sbtScalaBinaryVersion: String
  ): Future[ModuleStatus] =
    check(module, Option(sbtBinaryVersion -> sbtScalaBinaryVersion))

  private[this] def check(module: ModuleID, sbtSettings: Option[(String, String)] = None): Future[ModuleStatus] = {
    val mv = new DefaultArtifactVersion(module.revision)
    groups.foldLeft(Future.successful(Seq.empty[String] -> Option.empty[ModuleStatus])) { (future, group) =>
      future.flatMap {
        case (_, opt@Some(_)) => Future.successful(Seq.empty[String] -> opt)
        case (errors, _) =>
          group.getVersions(module, sbtSettings).map {
            case Nil => errors -> None
            case versions =>
              val (max: ArtifactVersion, status: Status.Value) = getModuleStatus(mv, versions)
              Seq.empty[String] -> Option(ModuleStatus(module, status, max.toString))
          } recover {
            case NonFatal(_: ArtifactNotFoundException) => errors -> None
            case NonFatal(t: MultiException) => (errors ++ t.getMessages) -> None
            case NonFatal(t) => (errors :+ t.getLocalizedMessage) -> None
          }
      }
    } map {
      case (_, Some(status)) => status
      case (errors, _) if errors.nonEmpty => ModuleStatus(module, Status.Error, errors)
      case _ => ModuleStatus(module, Status.NotFound)
    }
  }

  private def getModuleStatus(mv: DefaultArtifactVersion, versions: Seq[ArtifactVersion]) = {
    val latestOpt = Versions.latestRelease(versions)
    latestOpt match {
      case None => mv -> Status.NotFound
      case Some(latest) =>
        val status = if (!Versions.isReleaseVersion(mv)) {
          Status.Unreleased
        } else {
          mv.compareTo(latest) match {
            case 0 | 1 => Status.Success
            case _ => Status.Expired
          }
        }
        (latest, status)
    }
  }

  private[this] def getLoaderGroups(resolvers: Seq[Resolver]): Seq[MetadataLoaderGroup] = {
    val loaders: Seq[MetadataLoader] = resolvers.map {
      case repo: MavenRepository =>
        val url = repo.root
        if (isRemote(url)) {
          scala.util.Try(new java.net.URL(url)) match {
            case Failure(e) => logger.err(s"""Invalid URL "$url" for Maven repository: ${e.getMessage}"""); None
            case Success(_) => Option(new MavenRepoMetadataLoader(url))
          }
        } else {
          None
        }
      case repo: URLRepository =>
        val patterns = repo.patterns.ivyPatterns
        if (patterns.forall(isRemote)) {
          Option(new IvyPatternsMetadataLoader(patterns))
        } else {
          None
        }
      case _ => None
    } collect { case Some(loader) => loader }
    val mavenSearchMaxRows = 100
    Seq(
      MetadataLoaderGroup(scalaVersion, scalaBinaryVersion, loaders: _*),
      MetadataLoaderGroup(scalaVersion, scalaBinaryVersion, MavenSearchMetadataLoader(mavenSearchMaxRows))
    )
  }

  private[this] def isRemote(url: String): Boolean =
    !url.startsWith("file:") && !url.startsWith("jar:")

}
