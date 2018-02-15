package org.jmotor.sbt.resolver

import java.net.URL
import java.nio.file.Paths

import org.apache.maven.artifact.versioning.{ ArtifactVersion, DefaultArtifactVersion }
import org.asynchttpclient.Realm.AuthScheme
import org.asynchttpclient.{ AsyncHttpClient, Realm }
import org.jmotor.artifact.Versions
import org.jmotor.artifact.metadata.MetadataLoader
import org.jmotor.artifact.metadata.loader.{ IvyPatternsMetadataLoader, MavenRepoMetadataLoader, MavenSearchMetadataLoader }
import org.jmotor.sbt.model.{ ModuleStatus, Status }
import sbt.Credentials
import sbt.librarymanagement.{ MavenRepository, ModuleID, Resolver, URLRepository }

import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{ Await, ExecutionContext, Future, Promise }
import scala.util.Success
import scala.util.control.NonFatal

/**
 * Component:
 * Description:
 * Date: 2018/2/9
 *
 * @author AI
 */
class VersionResolverImpl(resolvers: Seq[Resolver], credentials: Seq[Credentials]) extends VersionResolver {

  private[this] implicit lazy val client: AsyncHttpClient = {
    import org.asynchttpclient.Dsl._
    val maxConnections = 50
    asyncHttpClient(config().setMaxConnectionsPerHost(maxConnections))
  }

  private[this] lazy val groups = getLoaderGroups(resolvers, credentials)

  override def checkForUpdates(module: ModuleID): ModuleStatus = check(module)

  override def checkPluginForUpdates(module: ModuleID, sbtVersion: String, scalaVersion: String): ModuleStatus = {
    check(module, Option(sbtVersion -> scalaVersion))
  }

  private[this] def check(module: ModuleID, sbtSettings: Option[(String, String)] = None): ModuleStatus = {
    val mv = new DefaultArtifactVersion(module.revision)
    val released = if (Option(mv.getQualifier).isDefined) {
      !Versions.UNRELEASED.exists(q ⇒ mv.getQualifier.toLowerCase.contains(q))
    } else {
      true
    }
    val qualifierOpt = if (released && Option(mv.getQualifier).isDefined) Option(mv.getQualifier) else None
    val errors = new ListBuffer[String]
    groups.foreach { group ⇒
      try {
        val versions = group.getVersions(module, sbtSettings)
        if (versions.nonEmpty) {
          val (max: ArtifactVersion, status: Status.Value) = getModuleStatus(mv, released, qualifierOpt, versions)
          return ModuleStatus(module, status, max.toString, None)
        }
      } catch {
        case NonFatal(t) ⇒ errors += t.getLocalizedMessage
      }
    }
    if (errors.nonEmpty) {
      ModuleStatus(module, Status.Error, "", Option(errors mkString "\n"))
    } else {
      ModuleStatus(module, Status.NotFound, "", None)
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
      MetadataLoaderGroup(new MavenSearchMetadataLoader()),
      MetadataLoaderGroup(loaders: _*))
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

class MetadataLoaderGroup(loaders: Seq[MetadataLoader]) {

  def getVersions(module: ModuleID, sbtSettings: Option[(String, String)]): Seq[ArtifactVersion] = {
    if (loaders.lengthCompare(1) > 0) {
      val future = firstCompletedOf(loaders.map { loader ⇒
        loader.getVersions(module.organization, getArtifactId(loader, module, sbtSettings))
      })
      Await.result(future, 30.seconds)
    } else {
      loaders.headOption.fold(Seq.empty[ArtifactVersion]) { loader ⇒
        val future = loader.getVersions(module.organization, getArtifactId(loader, module, sbtSettings))
        Await.result(future, 10.seconds)
      }
    }
  }

  def firstCompletedOf(futures: TraversableOnce[Future[Seq[ArtifactVersion]]])
    (implicit executor: ExecutionContext): Future[Seq[ArtifactVersion]] = {
    val p = Promise[Seq[ArtifactVersion]]()
    futures foreach { future ⇒
      future.onComplete {
        case Success(r) if r.nonEmpty ⇒ p success r
        case _                        ⇒
      }(scala.concurrent.ExecutionContext.Implicits.global)
    }
    p.future
  }

  def getArtifactId(loader: MetadataLoader, module: ModuleID, sbtSettings: Option[(String, String)]): String = {
    loader match {
      case _: IvyPatternsMetadataLoader if sbtSettings.isDefined ⇒
        val settings = sbtSettings.get
        Paths.get(module.name, settings._2, settings._1).toString
      case _ ⇒ module.name
    }
  }

}

object MetadataLoaderGroup {

  def apply(loaders: MetadataLoader*): MetadataLoaderGroup = new MetadataLoaderGroup(loaders)

}

