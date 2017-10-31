package org.jmotor.sbt

import org.jmotor.sbt.util.ProgressBar
import sbt._
import sbt.Keys._

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
        val bar = new ProgressBar("[info] Checking", "[info] Done checking.")
        bar.start()
        val pluginUpdates = Reporter.pluginUpdates(thisProject.value)
        val globalPluginUpdates = Reporter.globalPluginUpdates(sbtBinaryVersion.value)
        val dependencyUpdates = Reporter.dependencyUpdates(libraryDependencies.value, scalaVersion.value, scalaBinaryVersion.value)
        bar.stop()
        val logger = streams.value.log
        if (globalPluginUpdates.nonEmpty) {
          logger.info("=================== Global Plugins ===================")
          globalPluginUpdates.foreach(util.Logger.log)
        }
        if (pluginUpdates.nonEmpty) {
          logger.info("====================== Plugins =======================")
          pluginUpdates.foreach(util.Logger.log)
        }
        logger.info("==================== Dependencies ====================")
        dependencyUpdates.foreach(util.Logger.log)
      })
  }
}
