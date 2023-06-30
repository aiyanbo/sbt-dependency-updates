package org.jmotor.sbt.artifact

import org.apache.maven.artifact.versioning.DefaultArtifactVersion
import org.scalatest.funsuite.AnyFunSuite

class VersionsSpec extends AnyFunSuite {
  test("test rc version") {
    assert(Versions.isReleaseVersion(new DefaultArtifactVersion("3.9.0")))
    assert(!Versions.isReleaseVersion(new DefaultArtifactVersion("3.9.0-rc-1")))
    assert(!Versions.isReleaseVersion(new DefaultArtifactVersion("2.0.0-RC2-1")))
  }

  test("get latest release") {
    val versions = Seq(
      new DefaultArtifactVersion("3.9.0"),
      new DefaultArtifactVersion("3.9.0-jre9"),
      new DefaultArtifactVersion("3.8.0-jre10")
    )
    assert(Versions.latestRelease(versions).get.toString == "3.9.0")
  }

  test("get latest version in unstable versions") {
    val versions = Seq(
      new DefaultArtifactVersion("2.6.17+48-86b000c3-SNAPSHOT"),
      new DefaultArtifactVersion("4.4.5+9-89b0e0d8-SNAPSHOT"),
      new DefaultArtifactVersion("5.0.0.Final-SNAPSHOT"),
    )
    assert(Versions.latestRelease(versions).isEmpty)
  }

  test("is released version") {
    val v1 = new DefaultArtifactVersion("3.2.0-SNAP10")
    assert(!Versions.isReleaseVersion(v1))
    val v2 = new DefaultArtifactVersion("3.2.0-PROD")
    assert(Versions.isReleaseVersion(v2))
    val v3 = new DefaultArtifactVersion("3.2.0-pr10")
    assert(!Versions.isReleaseVersion(v3))
    val v4 = new DefaultArtifactVersion("3.2.0-M1")
    assert(!Versions.isReleaseVersion(v4))
  }

  test("scala m version") {
    val v = new DefaultArtifactVersion("2.13.0-M4-pre-20d3c21")
    assert(!Versions.isReleaseVersion(v))
  }

  test("is jre qualifier") {
    assert(Versions.isJreQualifier("jre7"))
    assert(!Versions.isJreQualifier("jrep"))
  }

}
