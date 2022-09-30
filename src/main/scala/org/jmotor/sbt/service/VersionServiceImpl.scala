package org.jmotor.sbt.service

import org.apache.maven.artifact.versioning.{ArtifactVersion, DefaultArtifactVersion}
import org.jmotor.artifact.Versions
import org.jmotor.artifact.exception.ArtifactNotFoundException
import org.jmotor.artifact.metadata.MetadataLoader
import org.jmotor.artifact.metadata.loader.{IvyPatternsMetadataLoader, MavenRepoMetadataLoader, MavenSearchMetadataLoader}
import org.jmotor.sbt.dto.{ModuleStatus, Status}
import org.jmotor.sbt.exception.MultiException
import org.jmotor.sbt.metadata.MetadataLoaderGroup
import sbt.Credentials
import sbt.librarymanagement.{MavenRepository, ModuleID, Resolver, URLRepository}
import sbt.util.Logger

import java.net.URL
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NonFatal
import scala.util.{Failure, Success, Try}

/**
 * Component: Description: Date: 2018/2/9
 *
 * @author
 * AI
 */
class VersionServiceImpl(
                          logger: Logger,
                          scalaVersion: String,
                          scalaBinaryVersion: String,
                          resolvers: Seq[Resolver],
                          credentials: Seq[Credentials]
                        ) extends VersionService {

  private[this] lazy val groups = getLoaderGroups(resolvers, credentials)

  override def checkForUpdates(module: ModuleID): Future[ModuleStatus] = check(module)

  override def checkPluginForUpdates(
                                      module: ModuleID,
                                      sbtBinaryVersion: String,
                                      sbtScalaBinaryVersion: String
                                    ): Future[ModuleStatus] =
    check(module, Option(sbtBinaryVersion -> sbtScalaBinaryVersion))

  private[this] def check(module: ModuleID, sbtSettings: Option[(String, String)] = None): Future[ModuleStatus] = {
    val mv = new DefaultArtifactVersion(module.revision)
    val released = Versions.isReleaseVersion(mv)
    val qualifierOpt = if (released && Option(mv.getQualifier).isDefined) Option(mv.getQualifier) else None
    groups.foldLeft(Future.successful(Seq.empty[String] -> Option.empty[ModuleStatus])) { (future, group) =>
      future.flatMap {
        case (_, opt@Some(_)) => Future.successful(Seq.empty[String] -> opt)
        case (errors, _) =>
          group.getVersions(module, sbtSettings).map {
            case Nil => errors -> None
            case versions =>
              val (max: ArtifactVersion, status: Status.Value) = getModuleStatus(mv, released, qualifierOpt, versions)
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

  private def getModuleStatus(
                               mv: DefaultArtifactVersion,
                               released: Boolean,
                               qualifierOpt: Option[String],
                               versions: Seq[ArtifactVersion]
                             ) = {
    val releases = versions.filter(Versions.isReleaseVersion)
    val matches = qualifierOpt match {
      case None =>
        releases.filter { av =>
          Option(av.getQualifier) match {
            case None => true
            case Some(qualifier) => !Versions.isJreQualifier(qualifier)
          }
        }
      case Some(q) => releases.filter(av => Option(av.getQualifier).isDefined && q == av.getQualifier)
    }
    val max = matches.max
    val status = if (!released) {
      Status.Unreleased
    } else {
      mv.compareTo(max) match {
        case 0 | 1 => Status.Success
        case _ => Status.Expired
      }
    }
    (max, status)
  }

  private[this] def getLoaderGroups(
                                     resolvers: Seq[Resolver],
                                     credentials: Seq[Credentials]
                                   ): Seq[MetadataLoaderGroup] = {
    val loaders: Seq[MetadataLoader] = resolvers.map {
      case repo: MavenRepository =>
        val url = repo.root
        if (isRemote(url)) {
          Option(new MavenRepoMetadataLoader(url, getCredentials(url, credentials)))
        } else {
          None
        }
      case repo: URLRepository =>
        val patterns = repo.patterns.ivyPatterns
        if (patterns.forall(isRemote)) {
          Option(new IvyPatternsMetadataLoader(patterns, getCredentials(patterns.head, credentials)))
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

  private[this] def getCredentials(url: String, credentials: Seq[Credentials]): Option[String] = {
    val host = new URL(url).getHost
    Try {
      Credentials.forHost(credentials, host).map { c =>
        okhttp3.Credentials.basic(c.userName, c.passwd)
      }
    } match {
      case Success(r) => r
      case Failure(t) =>
        logger.warn(t.getLocalizedMessage)
        None
    }
  }

  private[this] def isRemote(url: String): Boolean = {
    url.startsWith("http://") || url.startsWith("https://")
  }

}
