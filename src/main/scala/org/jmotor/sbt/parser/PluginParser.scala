package org.jmotor.sbt.parser

import sbt.ModuleID

import scala.util.matching.Regex

object PluginParser {
  lazy val AddSbtPluginRegex: Regex = """addSbtPlugin\("([\w\.-]+)" *%{1,2} *"([\w\.-]+)"\ *% *"([\w\.-]+)"\)""".r

  def parse(lines: Seq[String]): Seq[ModuleID] = {
    lines.map(_.trim).filter { line ⇒
      line.nonEmpty && line.startsWith("addSbtPlugin")
    }.flatMap {
      case AddSbtPluginRegex(org, n, v) ⇒ Some(ModuleID(org, n, v))
      case _                            ⇒ None
    }
  }
}
