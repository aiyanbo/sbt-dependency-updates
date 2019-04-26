package org.jmotor.sbt.parser

import scala.util.matching.Regex

/**
 * Component:
 * Description:
 * Date: 2018/3/1
 *
 * @author AI
 */
object VersionParser {

  lazy val VersionRegex: Regex = """val ?(\w+) ?= ?"(.*)"""".r
  lazy val VersionsObjectRegex: Regex = """[\t ]*object ?Versions ?\{([^{]*)[\t ]*\}""".r

  def parseVersionLines(text: String): Array[String] = {
    (for (m ← VersionsObjectRegex.findFirstMatchIn(text)) yield m.group(1)) match {
      case None ⇒ Array.empty
      case Some(v) ⇒
        v.split("\n").map { line ⇒
          line.replace("\t", "").trim
        }.filter(_.nonEmpty)
    }
  }

}
