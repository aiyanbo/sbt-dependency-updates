# sbt-dependency-updates

[![Build Status](https://travis-ci.org/aiyanbo/sbt-dependency-updates.svg?branch=master)](https://travis-ci.org/aiyanbo/sbt-dependency-updates)
[![Latest Release](https://stack-badges.herokuapp.com/maven-central/org.jmotor.sbt/sbt-dependency-updates/latest.svg)](https://stack-badges.herokuapp.com/maven-central/org.jmotor.sbt/sbt-dependency-updates/latest)

sbt-dependency-updates 可以帮助 SBT 构建的工程检查所有依赖是否有可用的最新版本。这个功能及时提地提醒了需要升级的组件，帮助我们使用最新的组件构建稳定的，安全的应用。

## Usage

```scala

addSbtPlugin("org.jmotor.sbt" % "sbt-dependency-updates" % "1.0.1")

```

## Tasks

`dependencyUpdates`: 检查依赖是否有最新版本
