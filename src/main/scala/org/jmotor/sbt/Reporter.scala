package org.jmotor.sbt

import java.io.File

import org.jmotor.sbt.model.ModuleStatus
import org.jmotor.sbt.service.ModuleUpdatesService
import sbt.CrossVersion._
import sbt.{ModuleID, ResolvedProject}

import scala.util.{Failure, Success, Try}

/**
 * Component:
 * Description:
 * Date: 2017/2/14
 *
 * @author AI
 */
object Reporter {

  private[this] val addSbtPluginRegex = """addSbtPlugin\("([\w\.-]+)" *%{1,2} *"([\w\.-]+)"\ *% *"([\w\.-]+)"\)""".r

  def dependencyUpdates(dependencies: Seq[ModuleID], scalaVersion: String, scalaBinaryVersion: String): Seq[ModuleStatus] = {
    val fullNameDependencies = dependencies.map { m ⇒
      val remapVersion = m.crossVersion match {
        case Disabled  ⇒ None
        case b: Binary ⇒ Option(scalaBinaryVersion)
        case f: Full   ⇒ Option(scalaVersion)
      }
      val name = remapVersion.map(v ⇒ s"${m.name}_$v").getOrElse(m.name)
      m.copy(name = name)
    }
    ModuleUpdatesService.resolve(fullNameDependencies).sortBy(_.status.id)
  }

  def pluginUpdates(project: ResolvedProject): Seq[ModuleStatus] = {
    ModuleUpdatesService.resolve(plugins(project)).sortBy(_.status.id)
  }

  def plugins(project: ResolvedProject): Seq[ModuleID] = {
    Try(
      sbt.IO.readLines(new File(project.base.getAbsoluteFile + "/project/plugins.sbt"))
    ) match {
        case Success(lines) ⇒
          lines.filter { line ⇒
            val trimLine = line.trim
            trimLine.nonEmpty && trimLine.startsWith("addSbtPlugin")
          } map {
            case addSbtPluginRegex(org, n, v) ⇒ ModuleID(org, n, v)
          }
        case Failure(_) ⇒ Seq.empty[ModuleID]
      }
  }

}
