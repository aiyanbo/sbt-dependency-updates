sbtPlugin := true

organization := "org.jmotor.sbt"

name := "sbt-dependency-updates"

version := "1.1.9"

scalacOptions ++= Seq("-deprecation", "-unchecked")

libraryDependencies ++= Dependencies.dependencies
