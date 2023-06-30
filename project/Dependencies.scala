import sbt._

object Dependencies {

  object Versions {
    val fansi = "0.4.0"
    val guava = "32.0.1-jre"
    val scalaLibrary = "2.13.11"
    val scalafmtDynamic = "3.7.4"
    val scalatest = "3.2.16"
    val slf4jSimple = "2.0.7"
    val okhttp = "4.11.0"
    val jacksonModuleScala = "2.15.2"
    val scalaXml = "2.1.0"
    //    val scalaXml = "1.3.0"
    val mavenArtifact = "3.9.3"
  }

  object Compile {
    val fansi = "com.lihaoyi" %% "fansi" % Versions.fansi
    val guava = "com.google.guava" % "guava" % Versions.guava
    val slf4jSimple = "org.slf4j" % "slf4j-simple" % Versions.slf4jSimple
    val okhttp = "com.squareup.okhttp3" % "okhttp" % Versions.okhttp
    val scalafmt = "org.scalameta" %% "scalafmt-dynamic" % Versions.scalafmtDynamic
    val scalaXml = "org.scala-lang.modules" %% "scala-xml" % Versions.scalaXml
    val mavenArtifact = "org.apache.maven" % "maven-artifact" % Versions.mavenArtifact
    val jacksonModuleScala = "com.fasterxml.jackson.module" %% "jackson-module-scala" % Versions.jacksonModuleScala
  }

  object Tests {
    val scalaTest: ModuleID = "org.scalatest" %% "scalatest" % Versions.scalatest % Test
  }

  import Compile._

  lazy val dependencies: Seq[ModuleID] = Seq(okhttp, fansi, guava, slf4jSimple, scalafmt,
    scalaXml, mavenArtifact, jacksonModuleScala, Tests.scalaTest)

}
