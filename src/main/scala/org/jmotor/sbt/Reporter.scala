package org.jmotor.sbt

import java.nio.file.{Files, Path, Paths}

import org.jmotor.sbt.dto.ModuleStatus
import org.jmotor.sbt.parser.PluginParser
import org.jmotor.sbt.service.VersionService
import sbt.{ModuleID, ResolvedProject}

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

/**
 * Component:
 * Description:
 * Date: 2017/2/14
 *
 * @author AI
 */
class Reporter(versionService: VersionService, organizationsToInclude: Seq[String] = Seq()) {

  def dependencyUpdates(dependencies: Seq[ModuleID]): Future[Seq[ModuleStatus]] = {

    Future.traverse(dependencies.filter(filterByOrganization))(versionService.checkForUpdates).map(_.sortBy(_.status.id))
  }

  def pluginUpdates(sbtBinaryVersion: String, project: ResolvedProject): Future[Seq[ModuleStatus]] = {
    val dir = Paths.get(project.base.getPath, "project")
    val sbtScalaBinaryVersion = getSbtScalaBinaryVersion(sbtBinaryVersion)
    Future.traverse(plugins(dir)) { module ⇒
      versionService.checkPluginForUpdates(module, sbtBinaryVersion, sbtScalaBinaryVersion)
    }.map(_.sortBy(_.status.id))
  }

  def globalPluginUpdates(sbtBinaryVersion: String): Future[Seq[ModuleStatus]] = {
    val dir = Paths.get(System.getProperty("user.home"), ".sbt", sbtBinaryVersion, "plugins")
    val sbtScalaBinaryVersion = getSbtScalaBinaryVersion(sbtBinaryVersion)
    Future.traverse(plugins(dir)) { module ⇒
      versionService.checkPluginForUpdates(module, sbtBinaryVersion, sbtScalaBinaryVersion)
    }.map(_.sortBy(_.status.id))
  }

  def plugins(dir: Path): Seq[ModuleID] = {
    Try {
      Files.newDirectoryStream(dir, "*.sbt").asScala.toSeq.flatMap { path ⇒
        Files.readAllLines(path).asScala
      }
    } match {
      case Success(lines) ⇒ PluginParser.parse(lines)
      case Failure(_)     ⇒ Seq.empty[ModuleID]
    }
  }.filter(filterByOrganization)

  private[sbt] def getSbtScalaBinaryVersion(sbtBinaryVersion: String): String = {
    sbtBinaryVersion match {
      case "1.0" ⇒ "2.12"
      case _     ⇒ "2.10"
    }
  }

  private def filterByOrganization(moduleId: ModuleID) = {
    organizationsToInclude.isEmpty || organizationsToInclude.contains(moduleId.organization)
  }
  
}



object Reporter {

  def apply(versionService: VersionService, organizationsToInclude: Seq[String] = Seq()): Reporter = new Reporter(versionService, organizationsToInclude)

}
