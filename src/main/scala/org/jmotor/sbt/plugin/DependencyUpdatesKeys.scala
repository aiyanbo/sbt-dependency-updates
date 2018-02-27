package org.jmotor.sbt.plugin

import sbt.{ TaskKey, taskKey }

/**
 * Component:
 * Description:
 * Date: 2018/2/27
 *
 * @author AI
 */
trait DependencyUpdatesKeys {

  lazy val dependencyUpdates: TaskKey[Unit] = taskKey[Unit]("Check for updates")

}

object DependencyUpdatesKeys extends DependencyUpdatesKeys
