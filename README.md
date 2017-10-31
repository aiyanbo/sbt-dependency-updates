# sbt-dependency-updates

[![Build Status](https://travis-ci.org/aiyanbo/sbt-dependency-updates.svg?branch=master)](https://travis-ci.org/aiyanbo/sbt-dependency-updates)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.jmotor.sbt/sbt-dependency-updates/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.jmotor.sbt/sbt-dependency-updates)

Display your SBT project's dependency updates.

![Dome](https://raw.githubusercontent.com/aiyanbo/paper-images/master/sbt-dependency-updates.gif)

## Installation

Add the following line to one of these files:
- The project-specific file at `project/plugins.sbt`
- Your global file at `~/.sbt/1.0/plugins/plugins.sbt` for sbt **1.0**

```scala

addSbtPlugin("org.jmotor.sbt" % "sbt-dependency-updates" % "1.1.0")

```

or `~/.sbt/0.13/plugins/plugins.sbt` for sbt **0.13**

```scala

addSbtPlugin("org.jmotor.sbt" % "sbt-dependency-updates" % "1.0.7")

```

## Tasks

`dependencyUpdates`: show a list of project dependencies that can be updated
