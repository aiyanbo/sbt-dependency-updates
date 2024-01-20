import sbt._

object Dependencies {

  object Versions {
    val fansi              = "0.4.0"
    val guava              = "33.0.0-jre"
    val jacksonModuleScala = "2.16.1"
    val mavenArtifact      = "3.9.6"
    val okhttp             = "4.12.0"
    val scalaLibrary       = "2.13.12"
    val scalaXml           = "2.2.0"
    val scalafmtDynamic    = "3.7.17"
    val scalatest          = "3.2.17"
    val slf4jSimple        = "2.0.11"
  }

  object Compile {
    val fansi              = "com.lihaoyi"                  %% "fansi"                % Versions.fansi
    val guava              = "com.google.guava"              % "guava"                % Versions.guava
    val slf4jSimple        = "org.slf4j"                     % "slf4j-simple"         % Versions.slf4jSimple
    val okhttp             = "com.squareup.okhttp3"          % "okhttp"               % Versions.okhttp
    val scalafmt           = "org.scalameta"                %% "scalafmt-dynamic"     % Versions.scalafmtDynamic
    val scalaXml           = "org.scala-lang.modules"       %% "scala-xml"            % Versions.scalaXml
    val mavenArtifact      = "org.apache.maven"              % "maven-artifact"       % Versions.mavenArtifact
    val jacksonModuleScala = "com.fasterxml.jackson.module" %% "jackson-module-scala" % Versions.jacksonModuleScala
  }

  object Tests {
    val scalaTest: ModuleID = "org.scalatest" %% "scalatest" % Versions.scalatest % Test
  }

  import Compile._

  lazy val dependencies: Seq[ModuleID] =
    Seq(okhttp, fansi, guava, slf4jSimple, scalafmt, scalaXml, mavenArtifact, jacksonModuleScala, Tests.scalaTest)

}
