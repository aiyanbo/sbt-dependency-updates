package org.jmotor.sbt

import org.scalatest.funsuite.AnyFunSuite

/**
 * Component:
 * Description:
 * Date: 2018/4/28
 *
 * @author AI
 */
class UpdatesSpec extends AnyFunSuite {

  test("mapping names") {
    val mappings = Map(
      "log4j-api" -> "log4j2",
      "undertow.*" -> "undertow",
      "akka.*" -> "akka",
      "akka-http" -> "akkaHttp")
    assert("akka" == Updates.mappingModuleName("akka-actor", mappings))
    assert("akkaHttp" == Updates.mappingModuleName("akka-http", mappings))
    assert("log4j2" == Updates.mappingModuleName("log4j-api", mappings))
    assert("scalaUtils" == Updates.mappingModuleName("scala-utils", mappings))
    assert("undertow" == Updates.mappingModuleName("undertow-servlet", mappings))
  }

}
