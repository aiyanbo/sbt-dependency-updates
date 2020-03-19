package org.jmotor.sbt.util

import org.apache.maven.artifact.versioning.DefaultArtifactVersion
import org.jmotor.artifact.Versions
import org.scalatest.funsuite.AnyFunSuite

/**
 * Component:
 * Description:
 * Date: 2018/10/10
 *
 * @author AI
 */
class VersionSpec extends AnyFunSuite {

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
