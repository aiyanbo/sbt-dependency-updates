package org.jmotor.sbt.artifact

import org.apache.maven.artifact.versioning.{ArtifactVersion, DefaultArtifactVersion}

/**
 * Component: Description: Date: 2018/2/8
 *
 * @author
 *   AI
 */
object Versions {

  private final lazy val ReleaseFlags: Set[String]   = Set("prod")
  private final lazy val UnReleaseFlags: Set[String] = Set("pr", "m", "beta", "rc", "alpha", "snapshot", "snap")

  private[this] final lazy val jrePattern = s"jre\\d+".r.pattern

  //  private[this] final lazy val UnreleasedPatterns: Set[Pattern] =
  //    Versions.UnReleaseFlags.map(q => s".*$q[_-]?\\d+.*".r.pattern)

  // we consider this version final if:
  // - there is no un-release flag suffixing the version, and
  // - if the version has a qualifier, it is either a release flag or at least it's not an un-release flag
  def isReleaseVersion(version: ArtifactVersion): Boolean =
    !(UnReleaseFlags.exists(version.toString.toLowerCase.endsWith)) &&
      Option(version.getQualifier).map(_.toLowerCase).forall { q =>
        ReleaseFlags(q) || !isUnreleaseQualifier(q)
      }

  // disqualify if `qualifier` (or a substring of it) occurs in UnReleaseFlags, or if it looks like a build number
  private def isUnreleaseQualifier(qualifier: String): Boolean =
    UnReleaseFlags(qualifier) || UnReleaseFlags.exists(qualifier.contains) || isBuildNumber(qualifier)

  // consisting entirely of hex characters, possibly with one dash in between (e.g., 234-d1a2b53 or 1234abc)
  private def isBuildNumber(qualifier: String): Boolean =
    qualifier.matches("^[0-9a-f]+(-[0-9a-f]+)?$")

  def latestRelease(versions: Seq[ArtifactVersion]): Option[ArtifactVersion] = {
    val releases = versions.collect {
      case av if isReleaseVersion(av) =>
        Option(av.getQualifier).fold(av) {
          case q if isJreQualifier(q) => new DefaultArtifactVersion(av.toString.replace(q, ""))
          case _                      => av
        }
    }
    if (releases.nonEmpty) {
      Some(releases.max)
    } else {
      None
    }
  }

  def isJreQualifier(qualifier: String): Boolean =
    jrePattern.matcher(qualifier).matches()

}
