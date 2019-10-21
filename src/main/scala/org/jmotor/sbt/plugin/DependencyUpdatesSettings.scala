package org.jmotor.sbt.plugin

import java.util.concurrent.Callable

import org.jmotor.sbt.out.UpdatesPrinter
import org.jmotor.sbt.service.VersionService
import org.jmotor.sbt.util.ProgressBar
import org.jmotor.sbt.{Reporter, Updates}
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
    dependencyUpgradeComponentSorter := ComponentSorter.ByLength,
    dependencyUpgradeModuleNames := Map.empty[String, String],
    onlyIncludeOrganizations := Seq(),
    allowSnapshotVersions := false,
    dependencyUpdates := {
      val reporter = Reporter(
        VersionService(
          sLog.value, scalaVersion.value, scalaBinaryVersion.value, fullResolvers.value, credentials.value, allowSnapshots = allowSnapshotVersions.value),
        organizationsToInclude = onlyIncludeOrganizations.value)
      val bar = new ProgressBar("[info] Checking", "[info] Done checking.")
      bar.start()

      val futureDependencyUpdates = reporter.dependencyUpdates(libraryDependencies.value)
      val futureGlobalPluginUpdates = reporter.globalPluginUpdates(sbtBinaryVersion.value)
      val futurePluginUpdates = reporter.pluginUpdates(sbtBinaryVersion.value, thisProject.value)
      val pluginUpdates = Await.result(futurePluginUpdates, (thisProject.value.autoPlugins.size * 10).seconds)
      val dependencyUpdates = Await.result(futureDependencyUpdates, (libraryDependencies.value.size * 10).seconds)
      val globalPluginUpdates = Await.result(futureGlobalPluginUpdates, (thisProject.value.autoPlugins.size * 10).seconds)
      bar.stop()
      val lock = appConfiguration.value.provider().scalaProvider().launcher().globalLock()
      val lockFile = new File("../.updates.lock")
      lock(lockFile, new Callable[Unit] {
        override def call(): Unit = {
          UpdatesPrinter.printReporter(thisProject.value.id, pluginUpdates, globalPluginUpdates, dependencyUpdates)
        }
      })
    },
    dependencyUpgrade := {
      val logger = sLog.value
      val reporter = Reporter(
        VersionService(
          logger, scalaVersion.value, scalaBinaryVersion.value, fullResolvers.value, credentials.value, allowSnapshots = allowSnapshotVersions.value),
        organizationsToInclude = onlyIncludeOrganizations.value)
      val bar = new ProgressBar("[info] Upgrading", "[info] Done upgrading.")
      bar.start()

      val futureDependencyUpdates = reporter.dependencyUpdates(libraryDependencies.value)
      val futurePluginUpdates = reporter.pluginUpdates(sbtBinaryVersion.value, thisProject.value)
      val pluginUpdates = Await.result(futurePluginUpdates, (thisProject.value.autoPlugins.size * 10).seconds)
      val dependencyUpdates = Await.result(futureDependencyUpdates, (libraryDependencies.value.size * 10).seconds)
      bar.stop()
      val projectId = thisProject.value.id
      val lock = appConfiguration.value.provider().scalaProvider().launcher().globalLock()
      val lockFile = new File("../.upgrades.lock")
      if (dependencyUpdates.nonEmpty) {
        lock(lockFile, new Callable[Unit] {
          override def call(): Unit = {
            Updates.applyDependencyUpdates(
              thisProject.value,
              scalaVersion.value, dependencyUpdates, dependencyUpgradeModuleNames.value, dependencyUpgradeComponentSorter.value) match {
              case None ⇒ logger.error("can not found Dependencies.scala")
              case Some(size) ⇒ logger.success(s"$projectId: $size dependencies upgraded")
            }
          }
        })
      } else {
        logger.info(s"$projectId: dependencies nothing to upgrade")
      }
      if (pluginUpdates.nonEmpty) {
        lock(lockFile, new Callable[Unit] {
          override def call(): Unit = {
            val size = Updates.applyPluginUpdates(thisProject.value, scalaVersion.value, pluginUpdates)
            logger.success(s"$projectId: $size plugins upgraded")
          }
        })
      } else {
        logger.info(s"$projectId: plugins nothing to upgrade")
      }
    })


}
