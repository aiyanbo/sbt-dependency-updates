package org.jmotor.sbt.util

import fansi.Color._
import org.apache.ivy.util.StringUtils
import org.jmotor.sbt.dto.Status._
import org.jmotor.sbt.dto.{ ModuleStatus, Status }

/**
 * Component:
 * Description:
 * Date: 2016/12/24
 *
 * @author AI
 */
object LayoutPrinter {

  def printStatus(module: ModuleStatus): Unit = {
    val status = module.status
    lazy val errorMessages = if (module.errors.nonEmpty) module.errors.mkString("\n") else "updates error, please retry!"
    val (color, message) = status match {
      case Expired    ⇒ Yellow → s"${Blue("--->")} ${Red(module.lastVersion)}"
      case Unreleased ⇒ Yellow → s"${Blue("--->")} ${Red(module.lastVersion)}"
      case Success    ⇒ Green → Green("√")
      case Error      ⇒ Red → errorMessages
      case NotFound   ⇒ Red → Red("×")
      case s          ⇒ Red → Red(s"unknown status ${s.toString}")
    }
    val length = Status.values.foldLeft(0) { (l, s) ⇒
      val length = s.toString.length
      if (length > l) {
        length
      } else {
        l
      }
    }
    val level = status.toString + StringUtils.repeat(" ", length - status.toString.length)
    print(s"[${color(level)}] ${module.raw} $message \n")
  }

  def printReporter(projectId: String, plugins: Seq[ModuleStatus],
                    globalPlugins: Seq[ModuleStatus], dependencies: Seq[ModuleStatus]): Unit = {
    import fansi._
    val style = Color.LightBlue ++ Reversed.On ++ Bold.On
    val titleWidth = 80
    val projectTitleWidth = 100
    val separator = style(wrap(projectId, " ", projectTitleWidth))
    print(s"$separator \n")
    if (globalPlugins.nonEmpty) {
      print(s"[info] ${wrap("Global Plugins", "-", titleWidth)}\n")
      globalPlugins.foreach(printStatus)
    }
    if (plugins.nonEmpty) {
      print(s"[info]  ${wrap("Plugins", "-", titleWidth)}\n")
      plugins.foreach(printStatus)
    }
    print(s"[info] ${wrap("Dependencies", "-", titleWidth)}\n")
    dependencies.foreach(printStatus)
  }

  private[util] def wrap(content: String, wrapWith: String, width: Int): String = {
    val wrapLength = (width - content.length) / 2
    val range = 0 to wrapLength
    val wrapStr = range.map(_ ⇒ wrapWith).mkString
    s"$wrapStr $content $wrapStr"
  }

}
