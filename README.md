# sbt-dependency-updates

[![Build Status](https://travis-ci.org/aiyanbo/sbt-dependency-updates.svg?branch=master)](https://travis-ci.org/aiyanbo/sbt-dependency-updates)

sbt-dependency-updates 可以帮助 SBT 构建的工程检查所有依赖是否有可用的最新版本。这个功能及时提地提醒了需要升级的组件，帮助我们使用最新的组件构建稳定的，安全的应用。

## Status

1. 结果仅供参考
2. 检查内容不包含私有组件, 仅能检测到 search.maven.org 索引过的

## Usage

```scala

addSbtPlugin("org.jmotor.sbt" % "sbt-dependency-updates" % "1.0.4")

```

## Tasks

`dependencyUpdates`: 检查依赖是否有最新版本
