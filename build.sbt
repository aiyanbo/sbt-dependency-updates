sbtPlugin := true

organization := "org.jmotor.sbt"

name := "sbt-dependency-updates"

version := "1.1.3"

crossSbtVersions := Seq("1.0.3", "0.13.16")

scalacOptions ++= Seq("-deprecation", "-unchecked")

libraryDependencies ++= Dependencies.dependencies
