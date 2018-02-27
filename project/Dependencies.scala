import sbt._

object Dependencies {

  object Versions {
    val fansi = "0.2.5"
    val scala212 = "2.12.4"
    val scala211 = "2.11.11"
    val scalaTest = "3.0.5"
    val scalaLogging = "3.7.2"
    val slf4jSimple = "1.7.25"
    val artifactVersions = "1.0.1"
  }

  object Compile {
    val fansi = "com.lihaoyi" %% "fansi" % "0.2.5"
    val slf4jSimple = "org.slf4j" % "slf4j-simple" % Versions.slf4jSimple
    val artifactVersions = "org.jmotor.artifact" %% "artifact-versions" % Versions.artifactVersions
  }

  object Test {
    val scalaTest: ModuleID = "org.scalatest" %% "scalatest" % Versions.scalaTest % "test"
  }

  import Compile._

  lazy val dependencies = Seq(fansi, slf4jSimple, artifactVersions, Test.scalaTest)

}
