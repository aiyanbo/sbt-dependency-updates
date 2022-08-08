import sbt._

object Dependencies {

  object Versions {
    val fansi = "0.4.0"
    val scalafmt = "3.5.8"
    val guava = "31.1-jre"
    val scalatest = "3.2.13"
    val slf4jSimple = "1.7.36"
    val scalaLibrary = "2.13.8"
    val artifactVersions = "1.0.7"
  }

  object Compile {
    val fansi = "com.lihaoyi" %% "fansi" % Versions.fansi
    val guava = "com.google.guava" % "guava" % Versions.guava
    val slf4jSimple = "org.slf4j" % "slf4j-simple" % Versions.slf4jSimple
    val scalafmt = "org.scalameta" %% "scalafmt-dynamic" % Versions.scalafmt
    val artifactVersions = "org.jmotor.artifact" %% "artifact-versions" % Versions.artifactVersions
  }

  object Tests {
    val scalaTest: ModuleID = "org.scalatest" %% "scalatest" % Versions.scalatest % Test
  }

  import Compile._

  lazy val dependencies: Seq[ModuleID] = Seq(fansi, guava, slf4jSimple, scalafmt, artifactVersions, Tests.scalaTest)

}
