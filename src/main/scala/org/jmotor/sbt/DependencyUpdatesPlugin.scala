package org.jmotor.sbt

import org.jmotor.sbt.service.ModuleUpdatesService
import org.jmotor.sbt.util.ProgressBar
import sbt.Keys._
import sbt.{AutoPlugin, Compile, Configuration, IntegrationTest, Optional, PluginTrigger, Provided, Runtime, TaskKey, Test, inConfig, taskKey}

/**
 * Component:
 * Description:
 * Date: 2016/12/22
 *
 * @author AI
 */
object DependencyUpdatesPlugin extends AutoPlugin {

  override def trigger: PluginTrigger = allRequirements

  override def projectSettings: Seq[_root_.sbt.Def.Setting[_]] =
    Seq(Compile, Test, IntegrationTest, Runtime, Provided, Optional).flatMap(dependencyUpdatesForConfig)

  val dependencyUpdates: TaskKey[Unit] = taskKey[Unit]("Check for updates")

  def dependencyUpdatesForConfig(config: Configuration): Seq[_root_.sbt.Def.Setting[_]] = inConfig(config) {
    Seq(
      dependencyUpdates := {
        scalaBinaryVersion.value
        val dependencies = libraryDependencies.value
        val binaryVersion = scalaBinaryVersion.value
        update.value.configuration(config.name) foreach { report ⇒
          val modules = report.details.flatMap(_.modules.map(_.module))
          val _dependencies = dependencies.map { d ⇒
            val name = modules.find(m ⇒ m.organization == d.organization && m.revision == d.revision &&
              (m.name == d.name || m.name == s"${d.name}_$binaryVersion")).map(_.name)
            d.copy(name = name.getOrElse(d.name))
          }
          val bar = new ProgressBar("[info] Checking", "[info] Done checking.")
          bar.start()
          val status = ModuleUpdatesService.resolve(_dependencies).sortBy(_.status.id)
          bar.stop()
          status.foreach(util.Logger.log)
        }
      }
    )
  }
}
