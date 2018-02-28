package org.jmotor.sbt.plugin

import org.jmotor.sbt.{ Reporter, Updates }
import org.jmotor.sbt.layout.LayoutPrinter
import org.jmotor.sbt.service.VersionService
import org.jmotor.sbt.util.ProgressBar
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
        val versionService = VersionService(
          scalaVersion.value,
          scalaBinaryVersion.value, fullResolvers.value, credentials.value)
        val bar = new ProgressBar("[info] Checking", "[info] Done checking.")
        bar.start()
        val pluginUpdates = Reporter.pluginUpdates(sbtBinaryVersion.value, thisProject.value, versionService)
        val globalPluginUpdates = Reporter.globalPluginUpdates(sbtBinaryVersion.value, versionService)
        val dependencyUpdates = Reporter.dependencyUpdates(libraryDependencies.value, versionService)
        bar.stop()
        LayoutPrinter.printReporter(thisProject.value.id, pluginUpdates, globalPluginUpdates, dependencyUpdates)
      },
      dependencyUpgrade := {
        val versionService = VersionService(
          scalaVersion.value,
          scalaBinaryVersion.value, fullResolvers.value, credentials.value)
        val bar = new ProgressBar("[info] Upgrading", "[info] Done upgrading.")
        bar.start()
        val dependencyUpdates = Reporter.dependencyUpdates(libraryDependencies.value, versionService)
        bar.stop()
        val log = streams.value.log
        if (dependencyUpdates.nonEmpty) {
          Updates.applyUpdates(thisProject.value, scalaVersion.value, dependencyUpdates) match {
            case None       ⇒ log.error("can not found Dependencies.scala")
            case Some(size) ⇒ log.success(s"$size dependencies upgraded")
          }
        } else {
          log.info("nothing to upgrade")
        }
      })
  }

}
