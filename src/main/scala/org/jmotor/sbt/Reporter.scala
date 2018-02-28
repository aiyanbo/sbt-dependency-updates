package org.jmotor.sbt

import java.nio.file.{ Files, Path, Paths }

import org.jmotor.sbt.dto.ModuleStatus
import org.jmotor.sbt.parser.PluginParser
import org.jmotor.sbt.service.VersionService
import sbt.{ ModuleID, ResolvedProject }

import scala.collection.JavaConverters._
import scala.util.{ Failure, Success, Try }

/**
 * Component:
 * Description:
 * Date: 2017/2/14
 *
 * @author AI
 */
object Reporter {

  def dependencyUpdates(
    dependencies:   Seq[ModuleID],
    versionService: VersionService): Seq[ModuleStatus] = {
    dependencies map versionService.checkForUpdates sortBy (_.status.id)
  }

  def pluginUpdates(sbtBinaryVersion: String, project: ResolvedProject, versionService: VersionService): Seq[ModuleStatus] = {
    val dir = Paths.get(project.base.getPath, "project")
    val (sbtFullVersion, scalaFullVersion) = extractSbtFullVersions(sbtBinaryVersion)
    plugins(dir) map (p ⇒ versionService.checkPluginForUpdates(p, sbtFullVersion, scalaFullVersion)) sortBy (_.status.id)
  }

  def globalPluginUpdates(sbtBinaryVersion: String, versionService: VersionService): Seq[ModuleStatus] = {
    val dir = Paths.get(System.getProperty("user.home"), ".sbt", sbtBinaryVersion, "plugins")
    val (sbtFullVersion, scalaFullVersion) = extractSbtFullVersions(sbtBinaryVersion)
    plugins(dir) map (p ⇒ versionService.checkPluginForUpdates(p, sbtFullVersion, scalaFullVersion)) sortBy (_.status.id)
  }

  def plugins(dir: Path): Seq[ModuleID] = {
    Try {
      Files.newDirectoryStream(dir, "*.sbt").asScala.toSeq.flatMap { path ⇒
        Files.readAllLines(path).asScala
      }
    } match {
      case Success(lines) ⇒ PluginParser.parseLine(lines)
      case Failure(_)     ⇒ Seq.empty[ModuleID]
    }
  }

  private[sbt] def extractSbtFullVersions(sbtBinaryVersion: String): (String, String) = {
    val scalaFullVersion = "scala_" + (sbtBinaryVersion match {
      case "1.0" ⇒ "2.12"
      case _     ⇒ "2.10"
    })
    s"sbt_$sbtBinaryVersion" -> scalaFullVersion
  }

}
