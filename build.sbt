import Settings.Formatting

organization := "org.jmotor.sbt"

name := "sbt-dependency-updates"

version := "1.0.0-SNAPSHOT"

sbtPlugin := true

libraryDependencies += "org.asynchttpclient" % "async-http-client" % "2.0.24"

scalacOptions ++= Seq("-deprecation", "-unchecked")

Formatting.formatSettings
