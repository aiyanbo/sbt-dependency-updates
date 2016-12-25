package org.jmotor.sbt

import java.io.File

import org.jmotor.sbt.service.ModuleUpdatesService
import org.jmotor.sbt.util.ProgressBar
import sbt.Keys._
import sbt.{AutoPlugin, Compile, Configuration, IntegrationTest, ModuleID, Optional, PluginTrigger, Provided, Runtime, TaskKey, Test, inConfig, taskKey}

import scala.util.{Failure, Success, Try}

/**
 * Component:
 * Description:
 * Date: 2016/12/22
 *
 * @author AI
 */
object DependencyUpdatesPlugin extends AutoPlugin {

  private[this] val addSbtPluginRegex = """addSbtPlugin\("([\w\.-]+)" ?%{1,2} ?"([\w\.-]+)"\ ?% ?"([\w\.-]+)"\)""".r

  override def trigger: PluginTrigger = allRequirements

  override def projectSettings: Seq[_root_.sbt.Def.Setting[_]] =
    Seq(Compile, Test, IntegrationTest, Runtime, Provided, Optional).flatMap(dependencyUpdatesForConfig)

  val dependencyUpdates: TaskKey[Unit] = taskKey[Unit]("Check for updates")

  def dependencyUpdatesForConfig(config: Configuration): Seq[_root_.sbt.Def.Setting[_]] = inConfig(config) {
    Seq(
      dependencyUpdates := {
        scalaBinaryVersion.value
        val binaryVersion = scalaBinaryVersion.value
        val dependencies = update.value.configuration(config.name) map { report ⇒
          val modules = report.details.flatMap(_.modules.map(_.module))
          libraryDependencies.value.map { d ⇒
            val name = modules.find(m ⇒ m.organization == d.organization && m.revision == d.revision &&
              (m.name == d.name || m.name == s"${d.name}_$binaryVersion")).map(_.name)
            d.copy(name = name.getOrElse(d.name))
          }
        } getOrElse Seq.empty[ModuleID]
        val plugins = Try(
          sbt.IO.readLines(new File(thisProject.value.base.getAbsoluteFile + "/project/plugins.sbt"))
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
        val bar = new ProgressBar("[info] Checking", "[info] Done checking.")
        bar.start()
        val dependenciesStatus = ModuleUpdatesService.resolve(dependencies).sortBy(_.status.id)
        val pluginsStatus = ModuleUpdatesService.resolve(plugins).sortBy(_.status.id)
        bar.stop()
        val logger = streams.value.log
        if (pluginsStatus.nonEmpty) {
          logger.info("====================== Plugins =======================")
          pluginsStatus.foreach(util.Logger.log)
        }
        logger.info("==================== Dependencies ====================")
        dependenciesStatus.foreach(util.Logger.log)
      }
    )
  }
}
