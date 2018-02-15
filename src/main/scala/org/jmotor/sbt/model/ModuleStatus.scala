package org.jmotor.sbt.model

import org.jmotor.sbt.model.Status.Status
import sbt.librarymanagement.ModuleID

/**
 * Component:
 * Description:
 * Date: 2016/12/22
 *
 * @author AI
 */
final case class ModuleStatus(module: ModuleID, status: Status, lastVersion: String, error: Option[String]) {

  lazy val raw: String = s"${module.organization}:${module.name}:${module.revision}"

}
