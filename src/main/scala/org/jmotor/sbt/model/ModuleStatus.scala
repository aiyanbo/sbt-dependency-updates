package org.jmotor.sbt.model

/**
 * Component:
 * Description:
 * Date: 2016/12/22
 *
 * @author AI
 */
final case class ModuleStatus(org: String, name: String, version: String, status: String, lastVersion: String) {

  lazy val id: String = s"$org:$name:$version"

}
