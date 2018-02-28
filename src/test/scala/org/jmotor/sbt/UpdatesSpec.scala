package org.jmotor.sbt

import org.scalatest.FunSuite

/**
 * Component:
 * Description:
 * Date: 2018/2/28
 *
 * @author AI
 */
class UpdatesSpec extends FunSuite {

  test("extract versions") {
    val text =
      """
        |import sbt._
        |
        |object Dependencies {
        |
        |  object Versions {
        |    val fansi = "0.2.5"
        |    val guava = "24.0-jre"
        |    val scala212 = "2.12.4"
        |    val scala211 = "2.11.11"
        |    val scalaTest = "3.0.5"
        |    val scalaLogging = "3.7.2"
        |    val slf4jSimple = "1.7.25"
        |    val artifactVersions = "1.0.1"
        |  }
        |
        |  object Compile {
        |    val fansi = "com.lihaoyi" %% "fansi" % Versions.fansi
        |    val guava = "com.google.guava" % "guava" % Versions.guava
        |    val slf4jSimple = "org.slf4j" % "slf4j-simple" % Versions.slf4jSimple
        |    val artifactVersions = "org.jmotor.artifact" %% "artifact-versions" % Versions.artifactVersions
        |  }
        |
        |  object Test {
        |    val scalaTest: ModuleID = "org.scalatest" %% "scalatest" % Versions.scalaTest % "test"
        |  }
        |
        |  import Compile._
        |
        |  lazy val dependencies = Seq(fansi, guava, slf4jSimple, artifactVersions, Test.scalaTest)
        |
        |}
        |
      """.stripMargin
    val versions = Updates.extractVersionLines(text)
    assert(versions.contains("""val fansi = "0.2.5""""))
  }

}
