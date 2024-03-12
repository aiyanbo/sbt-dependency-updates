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

  def isReleaseVersion(version: ArtifactVersion): Boolean = {
    val fullVersion = version.toString.toLowerCase
    if (UnReleaseFlags.exists(f => fullVersion.endsWith(f))) {
      false
    } else {
      Option(version.getQualifier) match {
        case None => true
        case Some(qualifier) =>
          val q = qualifier.toLowerCase
          if (ReleaseFlags.contains(q)) {
            true
          } else {
            !(Versions.UnReleaseFlags.contains(q) || UnReleaseFlags.exists(f => q.contains(f)))
          }
      }
    }
  }

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
