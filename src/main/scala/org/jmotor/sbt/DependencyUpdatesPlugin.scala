package org.jmotor.sbt

import org.jmotor.sbt.service.ModuleUpdatesService
import sbt.Keys._
import fansi.Color._
import org.jmotor.sbt.model.ModuleStatus
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
        val logger = streams.value.log
        val dependencies = libraryDependencies.value
        val binaryVersion = scalaBinaryVersion.value
        update.value.configuration(config.name) foreach { report ⇒
          val modules = report.details.flatMap(_.modules.map(_.module))
          val _dependencies = dependencies.map { d ⇒
            val name = modules.find(m ⇒ m.organization == d.organization && m.revision == d.revision &&
              (m.name == d.name || m.name == s"${d.name}_$binaryVersion")).map(_.name)
            d.copy(name = name.getOrElse(d.name))
          }
          ModuleUpdatesService.resolve(_dependencies).sortBy(_.status).foreach {
            case s @ ModuleStatus(_, _, _, "success", _) ⇒
              logger.success(s"${s.id} is latest version")
            case s @ ModuleStatus(_, _, _, "expired", lv) ⇒
              logger.warn(s"${s.id} can upgrade to ${Red(lv)}")
            case s @ ModuleStatus(_, _, _, "not_found", _) ⇒
              logger.error(s"${s.id} ${LightGray("can not found")}")
            case s @ ModuleStatus(_, _, _, "unreleased", lv) ⇒
              logger.warn(s"${s.id} is ${Yellow("unreleased")}, the latest version is ${Red(lv)}")
            case s @ ModuleStatus(_, _, _, "error", _) ⇒
              logger.error(s"${s.id} updates error, please retry!")
            case _ ⇒
          }
        }
      }
    )
  }
}
