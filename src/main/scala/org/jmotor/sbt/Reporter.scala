package org.jmotor.sbt

import java.nio.file.{ Files, Path, Paths }

import org.jmotor.sbt.model.ModuleStatus
import org.jmotor.sbt.resolver.VersionResolver
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

  def dependencyUpdates(
    scalaVersion:       String,
    scalaBinaryVersion: String,
    dependencies:       Seq[ModuleID],
    resolver:           VersionResolver): Seq[ModuleStatus] = {
    val fullNameDependencies = dependencies.map { m ⇒
      val remapVersion = m.crossVersion match {
        case _: Disabled ⇒ None
        case _: Binary   ⇒ Option(scalaBinaryVersion)
        case _: Full     ⇒ Option(scalaVersion)
      }
      val name = remapVersion.map(v ⇒ s"${m.name}_$v").getOrElse(m.name)
      m.withName(name)
    }
    fullNameDependencies map resolver.checkForUpdates sortBy (_.status.id)
  }

  def pluginUpdates(project: ResolvedProject, resolver: VersionResolver, sbtVersion: String, scalaVersion: String): Seq[ModuleStatus] = {
    val dir = Paths.get(project.base.getAbsoluteFile + "/project/")
    plugins(dir) map (p ⇒ resolver.checkPluginForUpdates(p, sbtVersion, scalaVersion)) sortBy (_.status.id)
  }

  def globalPluginUpdates(sbtBinaryVersion: String, resolver: VersionResolver, sbtVersion: String, scalaVersion: String): Seq[ModuleStatus] = {
    val dir = Paths.get(s"${System.getProperty("user.home")}/.sbt/$sbtBinaryVersion/plugins/")
    plugins(dir) map (p ⇒ resolver.checkPluginForUpdates(p, sbtVersion, scalaVersion)) sortBy (_.status.id)
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
