package org.jmotor.sbt.plugin

import org.jmotor.sbt.Reporter
import org.jmotor.sbt.service.VersionService
import org.jmotor.sbt.util.{ LayoutPrinter, ProgressBar }
import sbt.Keys._
import sbt._

/**
 * Component:
 * Description:
 * Date: 2018/2/27
 *
 * @author AI
 */
object DependencyUpdatesSettings {

  import DependencyUpdatesKeys._

  def updatesSettings: Seq[Setting[_]] =
    Seq(Compile, Test, IntegrationTest, Runtime, Provided, Optional).flatMap(dependencyUpdatesForConfig)

  def dependencyUpdatesForConfig(config: Configuration): Seq[Setting[_]] = inConfig(config) {
    Seq(
      dependencyUpdates := {
        val versionService = VersionService(fullResolvers.value, credentials.value)
        val bar = new ProgressBar("[info] Checking", "[info] Done checking.")
        bar.start()
        val pluginUpdates = Reporter.pluginUpdates(sbtBinaryVersion.value, thisProject.value, versionService)
        val globalPluginUpdates = Reporter.globalPluginUpdates(sbtBinaryVersion.value, versionService)
        val dependencyUpdates = Reporter.dependencyUpdates(
          scalaVersion.value, scalaBinaryVersion.value, libraryDependencies.value, versionService)
        bar.stop()
        LayoutPrinter.printReporter(thisProject.value.id, pluginUpdates, globalPluginUpdates, dependencyUpdates)
      })
  }

}
