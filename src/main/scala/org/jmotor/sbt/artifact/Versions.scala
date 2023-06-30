package org.jmotor.sbt.artifact

import org.apache.maven.artifact.versioning.ArtifactVersion

import java.util.regex.Pattern

/**
 * Component: Description: Date: 2018/2/8
 *
 * @author
 *   AI
 */
object Versions {

  final lazy val UNRELEASED: Seq[String] = Seq("pr", "m", "beta", "rc", "alpha", "snapshot", "snap")

  private[this] final lazy val jrePattern = s"jre\\d+".r.pattern

  private[this] final lazy val UnreleasedPatterns: Seq[Pattern] =
    Versions.UNRELEASED.map(q => s"$q[_-]?\\d+.*".r.pattern)

  def isReleaseVersion(version: ArtifactVersion): Boolean =
    Option(version.getQualifier) match {
      case None => true
      case Some(qualifier) =>
        val q = qualifier.toLowerCase
        !(Versions.UNRELEASED.contains(q) || UnreleasedPatterns.exists(_.matcher(q).matches()))
    }

  def isJreQualifier(qualifier: String): Boolean = {
    jrePattern.matcher(qualifier).matches()
  }

}
