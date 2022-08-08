package sbtmod

import org.jmotor.sbt.plugin.ComponentSorter.ComponentSorter
import sbt.SettingKey
import sbt.TaskKey
import sbt.settingKey
import sbt.taskKey

/** @author
  *   AI 2020/3/9
  */
trait ModPluginKeys {

  lazy val modUpdates: TaskKey[Unit] = taskKey[Unit]("Check for updates")

  lazy val modUpgrade: TaskKey[Unit] =
    taskKey[Unit]("Check for updates and upgrade [Experimental]")

  lazy val modUpgradeComponentSorter: SettingKey[ComponentSorter] =
    settingKey[ComponentSorter]("Component sorter")

  lazy val modUpgradeModuleNames: SettingKey[Map[String, String]] =
    settingKey[Map[String, String]]("Module name mappings")

}

object ModPluginKeys extends ModPluginKeys
