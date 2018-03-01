package org.jmotor.sbt.plugin

import org.jmotor.sbt.{ Reporter, Updates }
import org.jmotor.sbt.out.UpdatesPrinter
import org.jmotor.sbt.service.VersionService
import org.jmotor.sbt.util.ProgressBar
import sbt.Keys._
import sbt._
import scala.concurrent.Await
import scala.concurrent.duration._

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
        val futureDependencyUpdates = Reporter.dependencyUpdates(libraryDependencies.value, versionService)
        val futureGlobalPluginUpdates = Reporter.globalPluginUpdates(sbtBinaryVersion.value, versionService)
        val futurePluginUpdates = Reporter.pluginUpdates(sbtBinaryVersion.value, thisProject.value, versionService)
        val pluginUpdates = Await.result(futurePluginUpdates, (thisProject.value.autoPlugins.size * 10).seconds)
        val dependencyUpdates = Await.result(futureDependencyUpdates, (libraryDependencies.value.size * 10).seconds)
        val globalPluginUpdates = Await.result(futureGlobalPluginUpdates, (thisProject.value.autoPlugins.size * 10).seconds)
        bar.stop()
        UpdatesPrinter.printReporter(thisProject.value.id, pluginUpdates, globalPluginUpdates, dependencyUpdates)
      },
      dependencyUpgrade := {
        val versionService = VersionService(
          scalaVersion.value,
          scalaBinaryVersion.value, fullResolvers.value, credentials.value)
        val bar = new ProgressBar("[info] Upgrading", "[info] Done upgrading.")
        bar.start()
        val futureDependencyUpdates = Reporter.dependencyUpdates(libraryDependencies.value, versionService)
        val dependencyUpdates = Await.result(futureDependencyUpdates, (libraryDependencies.value.size * 10).seconds)
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
