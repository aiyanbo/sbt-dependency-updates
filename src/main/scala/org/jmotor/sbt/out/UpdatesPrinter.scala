package org.jmotor.sbt.out

import fansi.Color.*
import org.jmotor.sbt.dto.Status.*
import org.jmotor.sbt.dto.{ModuleStatus, Status}

import scala.util.Properties.lineSeparator as br

/**
 * Component: Description: Date: 2016/12/24
 *
 * @author
 *   AI
 */
object UpdatesPrinter {

  def printReporter(
    projectId: String,
    plugins: Seq[ModuleStatus],
    globalPlugins: Seq[ModuleStatus],
    dependencies: Seq[ModuleStatus]
  ): Unit = {
    import fansi.*
    val style             = Color.LightBlue ++ Reversed.On ++ Bold.On
    val titleWidth        = 80
    val projectTitleWidth = 100
    val separator         = style(wrap(projectId, " ", projectTitleWidth))

    val setsWithLabels = Seq(
      "Global Plugins" -> globalPlugins,
      "Plugins"        -> plugins,
      "Dependencies"   -> dependencies
    )

    val report = setsWithLabels.foldLeft(separator)((out, data) =>
      if (data._2.nonEmpty)
        s"""|$out
            |[info] ${wrap(data._1, "-", titleWidth)}
            |${data._2.map(statusLine).mkString(br)}""".stripMargin
      else out
    )

    println(report)
  }

  private[out] def wrap(content: String, wrapWith: String, width: Int): String = {
    val spacedContent = s" $content "
    val contentLength = spacedContent.length
    (wrapWith * width).patch((width - contentLength) / 2, spacedContent, contentLength)
  }

  def statusLine(module: ModuleStatus): String = {
    val status = module.status
    lazy val errorMessages =
      if (module.errors.nonEmpty) s"$br${module.errors.mkString(br)}$br" else "updates error, please retry!"
    val (color, message) = status match {
      case Expired | Unreleased => Yellow -> s"${Blue("--->")} ${Red(module.lastVersion)}"
      case Success              => Green  -> Green("√")
      case Error                => Red    -> errorMessages
      case NotFound             => Red    -> Red("×")
      case s                    => Red    -> Red(s"unknown status ${s.toString}")
    }
    val padding = Status.values.map(_.toString.length).max - status.toString.length
    val level   = s"$status${" " * padding}"
    s"[${color(level)}] ${module.raw} $message"
  }

}
