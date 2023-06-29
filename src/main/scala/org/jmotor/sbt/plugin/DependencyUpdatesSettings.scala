package org.jmotor.sbt.plugin

import org.jmotor.sbt.out.UpdatesPrinter
import org.jmotor.sbt.service.VersionService
import org.jmotor.sbt.{Reporter, Updates}
import sbt.*
import sbt.Keys.*

import java.util.concurrent.Callable
import scala.concurrent.Await
import scala.concurrent.duration.*

/**
 * Component: Description: Date: 2018/2/27
 *
 * @author
 *   AI
 */
object DependencyUpdatesSettings {

  import DependencyUpdatesKeys.*

  def updatesSettings: Seq[Setting[_]] = Seq(
    dependencyUpgradeComponentSorter := ComponentSorter.ByAlphabetically,
    dependencyUpgradeModuleNames     := Map.empty[String, String],
    dependencyUpdates := {
      val reporter = Reporter(
        VersionService(sLog.value, scalaVersion.value, scalaBinaryVersion.value, fullResolvers.value, credentials.value)
      )
      val futureDependencyUpdates   = reporter.dependencyUpdates(libraryDependencies.value)
      val futureGlobalPluginUpdates = reporter.globalPluginUpdates(sbtBinaryVersion.value)
      val futurePluginUpdates       = reporter.pluginUpdates(sbtBinaryVersion.value, thisProject.value)
      val pluginUpdates             = Await.result(futurePluginUpdates, (thisProject.value.autoPlugins.size * 10).seconds)
      val dependencyUpdates         = Await.result(futureDependencyUpdates, (libraryDependencies.value.size * 10).seconds)
      val globalPluginUpdates =
        Await.result(futureGlobalPluginUpdates, (thisProject.value.autoPlugins.size * 10).seconds)
      val lock     = appConfiguration.value.provider().scalaProvider().launcher().globalLock()
      val lockFile = new File("../.updates.lock")
      lock(
        lockFile,
        new Callable[Unit] {
          override def call(): Unit =
            UpdatesPrinter.printReporter(thisProject.value.id, pluginUpdates, globalPluginUpdates, dependencyUpdates)
        }
      )
    },
    dependencyUpgrade := {
      val logger = sLog.value
      val reporter = Reporter(
        VersionService(logger, scalaVersion.value, scalaBinaryVersion.value, fullResolvers.value, credentials.value)
      )
      val futureDependencyUpdates = reporter.dependencyUpdates(libraryDependencies.value)
      val futurePluginUpdates     = reporter.pluginUpdates(sbtBinaryVersion.value, thisProject.value)
      val pluginUpdates           = Await.result(futurePluginUpdates, (thisProject.value.autoPlugins.size * 10).seconds)
      val dependencyUpdates       = Await.result(futureDependencyUpdates, (libraryDependencies.value.size * 10).seconds)
      val projectId               = thisProject.value.id
      val lock                    = appConfiguration.value.provider().scalaProvider().launcher().globalLock()
      val lockFile                = new File("../.upgrades.lock")
      if (dependencyUpdates.nonEmpty) {
        lock(
          lockFile,
          new Callable[Unit] {
            override def call(): Unit =
              Updates.applyDependencyUpdates(
                thisProject.value,
                scalaVersion.value,
                dependencyUpdates,
                dependencyUpgradeModuleNames.value,
                dependencyUpgradeComponentSorter.value
              ) match {
                case None       => logger.error("can not found Dependencies.scala")
                case Some(size) => logger.success(s"$projectId: $size dependencies upgraded")
              }
          }
        )
      } else {
        logger.info(s"$projectId: dependencies nothing to upgrade")
      }
      if (pluginUpdates.nonEmpty) {
        lock(
          lockFile,
          new Callable[Unit] {
            override def call(): Unit = {
              val size = Updates.applyPluginUpdates(thisProject.value, scalaVersion.value, pluginUpdates)
              logger.success(s"$projectId: $size plugins upgraded")
            }
          }
        )
      } else {
        logger.info(s"$projectId: plugins nothing to upgrade")
      }
    }
  )

}
