package org.jmotor.sbt.plugin

import sbt._

/** Component: Description: Date: 2016/12/22
  *
  * @author
  *   AI
  */
object DependencyUpdatesPlugin extends AutoPlugin {

  object autoImport extends DependencyUpdatesKeys

  override def trigger: PluginTrigger = allRequirements

  override def projectSettings: Seq[Setting[_]] = DependencyUpdatesSettings.updatesSettings

}
