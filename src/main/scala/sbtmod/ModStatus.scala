package sbtmod

import sbt.librarymanagement.ModuleID
import sbtmod.VersionStatus.VersionStatus

/** @author
  *   AI 2020/3/17
  */
final case class ModStatus(
    module: ModuleID,
    lastVersion: String,
    status: VersionStatus,
    errors: Seq[Throwable]
)

object VersionStatus extends Enumeration {

  type VersionStatus = Value

  val Success: Value = Value(1, "success")

  val Unreleased: Value = Value(2, "unreleased")

  val Expired: Value = Value(3, "expired")

  val NotFound: Value = Value(4, "not_found")

  val Error: Value = Value(5, "error")

}
