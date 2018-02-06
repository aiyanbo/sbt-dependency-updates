package org.jmotor.sbt

import java.nio.file.{ Files, Path, Paths }

import org.jmotor.sbt.model.ModuleStatus
import org.jmotor.sbt.service.ModuleUpdatesService
import org.jmotor.sbt.util.PluginParser
import sbt.CrossVersion._
import sbt.librarymanagement.Disabled
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

  def dependencyUpdates(dependencies: Seq[ModuleID], scalaVersion: String, scalaBinaryVersion: String): Seq[ModuleStatus] = {
    val fullNameDependencies = dependencies.map { m ⇒
      val remapVersion = m.crossVersion match {
        case _: Disabled ⇒ None
        case _: Binary   ⇒ Option(scalaBinaryVersion)
        case _: Full     ⇒ Option(scalaVersion)
      }
      val name = remapVersion.map(v ⇒ s"${m.name}_$v").getOrElse(m.name)
      m.withName(name)
    }
    ModuleUpdatesService.resolve(fullNameDependencies).sortBy(_.status.id)
  }

  def pluginUpdates(project: ResolvedProject): Seq[ModuleStatus] = {
    val dir = Paths.get(project.base.getAbsoluteFile + "/project/")
    ModuleUpdatesService.resolve(plugins(dir)).sortBy(_.status.id)
  }

  def globalPluginUpdates(sbtBinaryVersion: String): Seq[ModuleStatus] = {
    val dir = Paths.get(s"${System.getProperty("user.home")}/.sbt/$sbtBinaryVersion/plugins/")
    ModuleUpdatesService.resolve(plugins(dir)).sortBy(_.status.id)
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

}
