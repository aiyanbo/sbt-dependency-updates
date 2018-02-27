package org.jmotor.sbt.dto

/**
 * Component:
 * Description:
 * Date: 2016/12/24
 *
 * @author AI
 */
object Status extends Enumeration {

  type Status = Value

  val Success = Value(1, "success")

  val Unreleased = Value(2, "unreleased")

  val Expired = Value(3, "expired")

  val NotFound = Value(4, "not_found")

  val Error = Value(5, "error")

}
