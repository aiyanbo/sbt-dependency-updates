import sbt._

object Dependencies {

  object Versions {
    val fansi = "0.2.5"
    val guava = "24.0-jre"
    val scalatest = "3.0.5"
    val scala212 = "2.12.4"
    val scala211 = "2.11.11"
    val scalariform = "0.2.6"
    val slf4jSimple = "1.7.25"
    val artifactVersions = "1.0.1"
  }

  object Compile {
    val fansi = "com.lihaoyi" %% "fansi" % Versions.fansi
    val guava = "com.google.guava" % "guava" % Versions.guava
    val slf4jSimple = "org.slf4j" % "slf4j-simple" % Versions.slf4jSimple
    val scalariform = "org.scalariform" %% "scalariform" % Versions.scalariform
    val artifactVersions = "org.jmotor.artifact" %% "artifact-versions" % Versions.artifactVersions
  }

  object Test {
    val scalaTest: ModuleID = "org.scalatest" %% "scalatest" % Versions.scalatest % "test"
  }

  import Compile._

  lazy val dependencies = Seq(fansi, guava, slf4jSimple, scalariform, artifactVersions, Test.scalaTest)

}
