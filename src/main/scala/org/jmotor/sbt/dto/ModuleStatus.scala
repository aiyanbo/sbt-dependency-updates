package org.jmotor.sbt.dto

import org.jmotor.sbt.dto.Status.Status
import sbt.librarymanagement.ModuleID

/** Component: Description: Date: 2016/12/22
  *
  * @author
  *   AI
  */
final case class ModuleStatus(module: ModuleID, status: Status, lastVersion: String, errors: Seq[String]) {

  lazy val raw: String = s"${module.organization}:${module.name}:${module.revision}"

}

object ModuleStatus {

  def apply(module: ModuleID, status: Status): ModuleStatus = {
    ModuleStatus(module, status, "", Seq.empty)
  }

  def apply(module: ModuleID, status: Status, lastVersion: String): ModuleStatus = {
    ModuleStatus(module, status, lastVersion, Seq.empty)
  }

  def apply(module: ModuleID, status: Status, errors: Seq[String]): ModuleStatus = {
    ModuleStatus(module, status, "", errors)
  }

}
