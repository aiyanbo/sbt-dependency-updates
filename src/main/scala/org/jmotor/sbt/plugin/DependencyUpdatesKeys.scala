package org.jmotor.sbt.plugin

import org.jmotor.sbt.plugin.ComponentSorter.ComponentSorter
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

  lazy val dependencyUpgradeComponentSorter: SettingKey[ComponentSorter] = settingKey[ComponentSorter]("Component sorter")

  lazy val onlyIncludeOrganizations: SettingKey[Seq[String]] = settingKey[Seq[String]]("Only include dependencies from this organizations")

  lazy val allowSnapshotVersions: SettingKey[Boolean] = settingKey[Boolean]("Accept SNAPSHOT versions as latest version")

  lazy val dependencyUpgradeModuleNames: SettingKey[Map[String, String]] = settingKey[Map[String, String]]("Module name mappings")

}

object DependencyUpdatesKeys extends DependencyUpdatesKeys
