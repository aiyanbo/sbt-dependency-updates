package org.jmotor.sbt.service

import java.net.URL

import org.apache.maven.artifact.versioning.{ ArtifactVersion, DefaultArtifactVersion }
import org.asynchttpclient.Realm.AuthScheme
import org.asynchttpclient.{ AsyncHttpClient, Realm }
import org.jmotor.artifact.Versions
import org.jmotor.artifact.exception.ArtifactNotFoundException
import org.jmotor.artifact.metadata.MetadataLoader
import org.jmotor.artifact.metadata.loader.{ IvyPatternsMetadataLoader, MavenRepoMetadataLoader, MavenSearchMetadataLoader }
import org.jmotor.sbt.dto.{ ModuleStatus, Status }
import org.jmotor.sbt.exception.MultiException
import org.jmotor.sbt.metadata.MetadataLoaderGroup
import sbt.Credentials
import sbt.librarymanagement.{ MavenRepository, ModuleID, Resolver, URLRepository }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NonFatal

/**
 * Component:
 * Description:
 * Date: 2018/2/9
 *
 * @author AI
 */
class VersionServiceImpl(
    scalaVersion:       String,
    scalaBinaryVersion: String,
    resolvers:          Seq[Resolver],
    credentials:        Seq[Credentials]) extends VersionService {

  private[this] implicit lazy val client: AsyncHttpClient = {
    import org.asynchttpclient.Dsl._
    val maxConnections = 50
    asyncHttpClient(config().setMaxConnectionsPerHost(maxConnections))
  }

  private[this] lazy val groups = getLoaderGroups(resolvers, credentials)

  override def checkForUpdates(module: ModuleID): Future[ModuleStatus] = check(module)

  override def checkPluginForUpdates(module: ModuleID, sbtBinaryVersion: String, sbtScalaBinaryVersion: String): Future[ModuleStatus] = {
    check(module, Option(sbtBinaryVersion -> sbtScalaBinaryVersion))
  }

  private[this] def check(module: ModuleID, sbtSettings: Option[(String, String)] = None): Future[ModuleStatus] = {
    val mv = new DefaultArtifactVersion(module.revision)
    val released = if (Option(mv.getQualifier).isDefined) {
      !Versions.UNRELEASED.exists(q ⇒ mv.getQualifier.toLowerCase.contains(q))
    } else {
      true
    }
    val qualifierOpt = if (released && Option(mv.getQualifier).isDefined) Option(mv.getQualifier) else None
    groups.foldLeft(Future.successful(Seq.empty[String] -> Option.empty[ModuleStatus])) { (future, group) ⇒
      future.flatMap {
        case (_, opt @ Some(_)) ⇒ Future.successful(Seq.empty[String] -> opt)
        case (errors, _) ⇒
          group.getVersions(module, sbtSettings).map {
            case Nil ⇒ errors -> None
            case versions ⇒
              val (max: ArtifactVersion, status: Status.Value) = getModuleStatus(mv, released, qualifierOpt, versions)
              Seq.empty[String] -> Option(ModuleStatus(module, status, max.toString))
          } recover {
            case NonFatal(_: ArtifactNotFoundException) ⇒ errors -> None
            case NonFatal(t: MultiException)            ⇒ (errors ++ t.getMessages) -> None
            case NonFatal(t)                            ⇒ (errors :+ t.getLocalizedMessage) -> None
          }
      }
    } map {
      case (_, Some(status))              ⇒ status
      case (errors, _) if errors.nonEmpty ⇒ ModuleStatus(module, Status.Error, errors)
      case _                              ⇒ ModuleStatus(module, Status.NotFound)
    }
  }

  private def getModuleStatus(mv: DefaultArtifactVersion, released: Boolean, qualifierOpt: Option[String], versions: Seq[ArtifactVersion]) = {
    val releases = versions.filter {
      case av if Option(av.getQualifier).isDefined ⇒
        !Versions.UNRELEASED.exists(q ⇒ av.getQualifier.toLowerCase.contains(q))
      case _ ⇒ true
    }
    val matches = qualifierOpt match {
      case None ⇒ releases
      case Some(q) ⇒ releases.collect {
        case av if Option(av.getQualifier).isDefined && q == av.getQualifier ⇒ av
      }
    }
    val max = matches.max
    val status = if (!released) {
      Status.Unreleased
    } else {
      mv.compareTo(max) match {
        case 0 | 1 ⇒ Status.Success
        case _     ⇒ Status.Expired
      }
    }
    (max, status)
  }

  private[this] def getLoaderGroups(resolvers: Seq[Resolver], credentials: Seq[Credentials]): Seq[MetadataLoaderGroup] = {
    val loaders: Seq[MetadataLoader] = new MavenSearchMetadataLoader() +: (resolvers.map {
      case repo: MavenRepository ⇒
        val url = repo.root
        if (isRemote(url)) {
          Option(new MavenRepoMetadataLoader(url, getRealm(url)))
        } else {
          None
        }
      case repo: URLRepository ⇒
        val patterns = repo.patterns.ivyPatterns
        if (patterns.forall(isRemote)) {
          Option(new IvyPatternsMetadataLoader(patterns, getRealm(patterns.head)))
        } else {
          None
        }
      case _ ⇒ None
    } collect { case Some(loader) ⇒ loader })
    Seq(
      MetadataLoaderGroup(scalaVersion, scalaBinaryVersion, new MavenSearchMetadataLoader()),
      MetadataLoaderGroup(scalaVersion, scalaBinaryVersion, loaders: _*))
  }

  private[this] def getRealm(url: String): Option[Realm] = {
    val host = new URL(url).getHost
    Credentials.forHost(credentials, host).map { c ⇒
      new Realm.Builder(c.userName, c.passwd).setScheme(AuthScheme.BASIC).build()
    }
  }

  private[this] def isRemote(url: String): Boolean = {
    url.startsWith("http://") || url.startsWith("https://")
  }

}
