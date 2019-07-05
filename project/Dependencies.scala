import sbt._

object Dependencies {

  object Versions {
    val fansi = "0.2.7"
    val guava = "28.0-jre"
    val scalatest = "3.0.8"
    val slf4jSimple = "1.7.26"
    val scalariform = "0.2.10"
    val scalaLibrary = "2.13.0"
    val artifactVersions = "1.0.6"
  }

  object Compile {
    val fansi = "com.lihaoyi" %% "fansi" % Versions.fansi
    val guava = "com.google.guava" % "guava" % Versions.guava
    val slf4jSimple = "org.slf4j" % "slf4j-simple" % Versions.slf4jSimple
    val scalariform = "org.scalariform" %% "scalariform" % Versions.scalariform
    val artifactVersions = "org.jmotor.artifact" %% "artifact-versions" % Versions.artifactVersions
  }

  object Tests {
    val scalaTest: ModuleID = "org.scalatest" %% "scalatest" % Versions.scalatest % Test
  }

  import Compile._

  lazy val dependencies: Seq[ModuleID] = Seq(fansi, guava, slf4jSimple, scalariform, artifactVersions, Tests.scalaTest)

}
