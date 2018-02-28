package org.jmotor.sbt.parser

import sbt.ModuleID

object PluginParser {
  private[this] val addSbtPluginRegex = """addSbtPlugin\("([\w\.-]+)" *%{1,2} *"([\w\.-]+)"\ *% *"([\w\.-]+)"\)""".r

  def parseLine(lines: Seq[String]): Seq[ModuleID] = {
    lines.map(_.trim).filter { line ⇒
      line.nonEmpty && line.startsWith("addSbtPlugin")
    }.flatMap {
      case addSbtPluginRegex(org, n, v) ⇒ Some(ModuleID(org, n, v))
      case _                            ⇒ None
    }
  }
}
