package org.jmotor.sbt.service

import org.scalatest.FunSuite
import sbt.ModuleID

/**
 * Component:
 * Description:
 * Date: 2016/12/24
 *
 * @author AI
 */
class ModuleUpdatesServiceSpec extends FunSuite {

  test("Resolve") {
    val status = ModuleUpdatesService.resolve(Seq(
      ModuleID("com.typesafe", "config", "1.3.0")
    ))
    assert(status.head.status == "expired")
  }

}
