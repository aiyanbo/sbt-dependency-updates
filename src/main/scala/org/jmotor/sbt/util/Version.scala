package org.jmotor.sbt.util

import org.apache.maven.artifact.versioning.ArtifactVersion
import org.jmotor.artifact.Versions

/**
 * Component:
 * Description:
 * Date: 2018/10/10
 *
 * @author AI
 */
object Version {

  private[this] lazy val jrePattern = s"jre\\d+".r.pattern

  private[this] lazy val unreleasedPatterns = Versions.UNRELEASED.map(q ⇒ s"$q\\d+".r.pattern)

  def isReleaseVersion(version: ArtifactVersion): Boolean = {
    Option(version.getQualifier) match {
      case None ⇒ true
      case Some(qualifier) ⇒
        val q = qualifier.toLowerCase
        !(Versions.UNRELEASED.contains(q) || unreleasedPatterns.exists(_.matcher(q).matches()))
    }
  }

  def isJreQualifier(qualifier: String): Boolean = {
    jrePattern.matcher(qualifier).matches()
  }

}
