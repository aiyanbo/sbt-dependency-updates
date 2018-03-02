package org.jmotor.sbt.plugin

import sbt._

/**
 * Component:
 * Description:
 * Date: 2018/2/27
 *
 * @author AI
 */
trait DependencyUpdatesKeys {

  lazy val dependencyUpdates: TaskKey[Unit] = taskKey[Unit]("Check for updates")

  lazy val dependencyUpgrade: TaskKey[Unit] = taskKey[Unit]("Check for updates and upgrade [Experimental]")

  lazy val dependencyUpdatesModuleNames: SettingKey[Map[String, String]] = settingKey[Map[String, String]]("Module name mappings")

}

object DependencyUpdatesKeys extends DependencyUpdatesKeys
