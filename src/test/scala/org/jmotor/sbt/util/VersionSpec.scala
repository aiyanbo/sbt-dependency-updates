package org.jmotor.sbt.util

import org.apache.maven.artifact.versioning.DefaultArtifactVersion
import org.scalatest.FunSuite

/**
 * Component:
 * Description:
 * Date: 2018/10/10
 *
 * @author AI
 */
class VersionSpec extends FunSuite {

  test("is released version") {
    val v1 = new DefaultArtifactVersion("3.2.0-SNAP10")
    assert(!Version.isReleaseVersion(v1))
    val v2 = new DefaultArtifactVersion("3.2.0-PROD")
    assert(Version.isReleaseVersion(v2))
    val v3 = new DefaultArtifactVersion("3.2.0-pr10")
    assert(!Version.isReleaseVersion(v3))
    val v4 = new DefaultArtifactVersion("3.2.0-M1")
    assert(!Version.isReleaseVersion(v4))
  }

  test("is jre qualifier") {
    assert(Version.isJreQualifier("jre7"))
    assert(!Version.isJreQualifier("jrep"))
  }

}
