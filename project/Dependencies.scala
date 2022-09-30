import sbt._

object Dependencies {

  object Versions {
    val artifactVersions = "1.0.9-SNAPSHOT"
    val fansi            = "0.4.0"
    val guava            = "31.1-jre"
    val scalaLibrary     = "2.13.9"
    val scalafmtDynamic  = "3.5.9"
    val scalatest        = "3.2.14"
    val slf4jSimple      = "2.0.3"
  }

  object Compile {
    val fansi            = "com.lihaoyi"         %% "fansi"             % Versions.fansi
    val guava            = "com.google.guava"     % "guava"             % Versions.guava
    val slf4jSimple      = "org.slf4j"            % "slf4j-simple"      % Versions.slf4jSimple
    val scalafmt         = "org.scalameta"       %% "scalafmt-dynamic"  % Versions.scalafmtDynamic
    val artifactVersions = "org.jmotor.artifact" %% "artifact-versions" % Versions.artifactVersions
  }

  object Tests {
    val scalaTest: ModuleID = "org.scalatest" %% "scalatest" % Versions.scalatest % Test
  }

  import Compile._

  lazy val dependencies: Seq[ModuleID] = Seq(fansi, guava, slf4jSimple, scalafmt, artifactVersions, Tests.scalaTest)

}
