import Settings.Formatting

scalaVersion := "2.10.6"

organization := "org.jmotor.sbt"

name := "sbt-dependency-updates"

version := "1.0.0-SNAPSHOT"

sbtPlugin := true

libraryDependencies ++= Seq(
  "com.lihaoyi" %% "fansi" % "0.2.3",
  "org.asynchttpclient" % "async-http-client" % "2.0.24"
)

scalacOptions ++= Seq("-deprecation", "-unchecked")

Formatting.formatSettings
