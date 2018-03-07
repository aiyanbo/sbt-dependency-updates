# sbt-dependency-updates

[![Build Status](https://travis-ci.org/aiyanbo/sbt-dependency-updates.svg?branch=master)](https://travis-ci.org/aiyanbo/sbt-dependency-updates)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.jmotor.sbt/sbt-dependency-updates/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.jmotor.sbt/sbt-dependency-updates)
[![Join the chat at https://gitter.im/aiyanbo/sbt-dependency-updates](https://badges.gitter.im/aiyanbo/sbt-dependency-updates.svg)](https://gitter.im/aiyanbo/sbt-dependency-updates?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

Display your SBT project's dependency updates.

![Dome](https://raw.githubusercontent.com/aiyanbo/paper-images/master/sbt-dependency-updates.gif)

## Installation

Add the following line to one of these files:
- The project-specific file at `project/plugins.sbt`
- Your global file at `~/.sbt/1.0/plugins/plugins.sbt` for sbt **1.0**

```scala

addSbtPlugin("org.jmotor.sbt" % "sbt-dependency-updates" % "1.1.5")

```

or `~/.sbt/0.13/plugins/plugins.sbt` for sbt **0.13**

```scala

addSbtPlugin("org.jmotor.sbt" % "sbt-dependency-updates" % "1.0.7")

```

## Tasks

- `dependencyUpdates`: show a list of project dependencies and plugins that can be updated
- `dependencyUpgrade`: `Experimental` upgrade of project dependencies and plugins that can be updated

### dependencyUpgrade

- Add `project/Dependencies.scala` in your root project
- `Dependencies.scala` as the following:

```scala
import sbt._

object Dependencies {

  // define versions, The variable name must be camel case by module name
  object Versions {
    val fansi = "0.2.5"
    val slf4j = "1.7.25"
    val guava = "24.0-jre"
    val scalatest = "3.0.5"
    val scala212 = "2.12.4"
    val scala211 = "2.11.11"
    val scalariform = "0.2.6"
    val scalaLogging = "3.7.2"
  }

  object Compile {
    val fansi = "com.lihaoyi" %% "fansi" % Versions.fansi
    val guava = "com.google.guava" % "guava" % Versions.guava
    val slf4jSimple = "org.slf4j" % "slf4j-simple" % Versions.slf4j
    val scalariform = "org.scalariform" %% "scalariform" % Versions.scalariform
  }

  object Test {
    val scalaTest: ModuleID = "org.scalatest" %% "scalatest" % Versions.scalatest % "test"
  }

  import Compile._

  lazy val dependencies = Seq(fansi, guava, slf4jSimple, scalariform, Test.scalaTest)

}

```

You can download an example project with this layout here:

- [sbt-simple-project](https://github.com/aiyanbo/sbt-simple-project)

## Settings

- `dependencyUpgradeModuleNames`: a setting to customize the mapping of module name

E.g. in `build.sbt` you can change configuration settings like this:

```scala
  dependencyUpgradeModuleNames := Map(
    "slf4j-simple" -> "slf4j"
  )
```
