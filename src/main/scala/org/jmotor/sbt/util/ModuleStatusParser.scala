package org.jmotor.sbt.util

/**
 * Component:
 * Description:
 * Date: 2016/12/22
 *
 * @author AI
 */
object ModuleStatusParser {
  private[this] val statusRegex = """"status": ?"(\w+)"""".r
  private[this] val versionRegex = """"current": ?"(.*)"""".r

  def parse(message: String): (String, String) = {
    val status = for (m ← statusRegex findFirstMatchIn message) yield m group 1
    val current = for (m ← versionRegex findFirstMatchIn message) yield m group 1
    (status.getOrElse("not_found"), current.getOrElse(""))
  }

}
