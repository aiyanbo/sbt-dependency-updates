package org.jmotor.sbt.model

import org.jmotor.sbt.model.Status.Status

/**
 * Component:
 * Description:
 * Date: 2016/12/22
 *
 * @author AI
 */
final case class ModuleStatus(org: String, name: String, version: String, status: Status, lastVersion: String) {

  lazy val id: String = s"$org:$name:$version"

}
