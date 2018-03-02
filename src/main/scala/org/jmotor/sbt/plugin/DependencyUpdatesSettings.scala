package org.jmotor.sbt.plugin

import org.jmotor.sbt.out.UpdatesPrinter
import org.jmotor.sbt.service.VersionService
import org.jmotor.sbt.util.ProgressBar
import org.jmotor.sbt.{ Reporter, Updates }
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

  def updatesSettings: Seq[Setting[_]] = Seq(
    dependencyUpdatesModuleNames := Map.empty[String, String],
    dependencyUpdates := {
      val reporter = Reporter(VersionService(
        scalaVersion.value, scalaBinaryVersion.value, fullResolvers.value, credentials.value))
      val bar = new ProgressBar("[info] Checking", "[info] Done checking.")
      bar.start()
      val futureDependencyUpdates = reporter.dependencyUpdates(libraryDependencies.value)
      val futureGlobalPluginUpdates = reporter.globalPluginUpdates(sbtBinaryVersion.value)
      val futurePluginUpdates = reporter.pluginUpdates(sbtBinaryVersion.value, thisProject.value)
      val pluginUpdates = Await.result(futurePluginUpdates, (thisProject.value.autoPlugins.size * 10).seconds)
      val dependencyUpdates = Await.result(futureDependencyUpdates, (libraryDependencies.value.size * 10).seconds)
      val globalPluginUpdates = Await.result(futureGlobalPluginUpdates, (thisProject.value.autoPlugins.size * 10).seconds)
      bar.stop()
      UpdatesPrinter.printReporter(thisProject.value.id, pluginUpdates, globalPluginUpdates, dependencyUpdates)
    },
    dependencyUpgrade := {
      val reporter = Reporter(VersionService(
        scalaVersion.value, scalaBinaryVersion.value, fullResolvers.value, credentials.value))
      val bar = new ProgressBar("[info] Upgrading", "[info] Done upgrading.")
      bar.start()
      val futureDependencyUpdates = reporter.dependencyUpdates(libraryDependencies.value)
      val futurePluginUpdates = reporter.pluginUpdates(sbtBinaryVersion.value, thisProject.value)
      val pluginUpdates = Await.result(futurePluginUpdates, (thisProject.value.autoPlugins.size * 10).seconds)
      val dependencyUpdates = Await.result(futureDependencyUpdates, (libraryDependencies.value.size * 10).seconds)
      bar.stop()
      val log = streams.value.log
      val projectId = thisProject.value.id
      if (dependencyUpdates.nonEmpty) {
        Updates.applyDependencyUpdates(
          thisProject.value,
          scalaVersion.value, dependencyUpdates, dependencyUpdatesModuleNames.value) match {
            case None       ⇒ log.error("can not found Dependencies.scala")
            case Some(size) ⇒ log.success(s"$projectId: $size dependencies upgraded")
          }
      } else {
        log.info(s"$projectId: dependencies nothing to upgrade")
      }
      if (pluginUpdates.nonEmpty) {
        val size = Updates.applyPluginUpdates(thisProject.value, scalaVersion.value, pluginUpdates)
        log.success(s"$projectId: $size plugins upgraded")
      } else {
        log.info(s"$projectId: plugins nothing to upgrade")
      }
    })

}
