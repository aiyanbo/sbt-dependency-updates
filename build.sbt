sbtPlugin := true

organization := "org.jmotor.sbt"

name := "sbt-dependency-updates"

version := "1.1.8"

scalacOptions ++= Seq("-deprecation", "-unchecked")

libraryDependencies ++= Dependencies.dependencies
