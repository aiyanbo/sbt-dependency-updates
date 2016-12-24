package org.jmotor.sbt.util

import org.scalatest.FunSuite

/**
 * Component:
 * Description:
 * Date: 2016/12/24
 *
 * @author AI
 */
class ModuleStatusParserSpec extends FunSuite {

  test("Parse") {
    val message = """{"status": "expired", "current": "1.3.1"}"""
    val (status, version) = ModuleStatusParser.parse(message)
    assert(status == "expired")
    assert(version == "1.3.1")
  }

}
