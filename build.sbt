sbtPlugin := true

organization := "org.jmotor.sbt"

name := "sbt-dependency-updates"

version := "1.1.7-SNAPSHOT"

scalacOptions ++= Seq("-deprecation", "-unchecked")

libraryDependencies ++= Dependencies.dependencies
