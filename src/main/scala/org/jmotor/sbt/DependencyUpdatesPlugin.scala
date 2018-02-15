package org.jmotor.sbt

import org.jmotor.sbt.resolver.VersionResolver
import org.jmotor.sbt.util.ProgressBar
import sbt._
import sbt.Keys._

/**
 * Component:
 * Description:
 * Date: 2016/12/22
 *
 * @author AI
 */
object DependencyUpdatesPlugin extends AutoPlugin {

  override def trigger: PluginTrigger = allRequirements

  override def projectSettings: Seq[_root_.sbt.Def.Setting[_]] =
    Seq(Compile, Test, IntegrationTest, Runtime, Provided, Optional).flatMap(dependencyUpdatesForConfig)

  val dependencyUpdates: TaskKey[Unit] = taskKey[Unit]("Check for updates")

  def dependencyUpdatesForConfig(config: Configuration): Seq[_root_.sbt.Def.Setting[_]] = inConfig(config) {
    Seq(
      dependencyUpdates := {
        val sbtVersionWithPrefix = s"sbt_${sbtBinaryVersion.value}"
        val sbtScalaVersionWithPrefix = "scala_" + (sbtBinaryVersion.value match {
          case "1.0" ⇒ "2.12"
          case _     ⇒ "2.10"
        })
        val resolver = VersionResolver(fullResolvers.value, credentials.value)
        val bar = new ProgressBar("[info] Checking", "[info] Done checking.")
        bar.start()
        val pluginUpdates = Reporter.pluginUpdates(
          thisProject.value,
          resolver, sbtVersionWithPrefix, sbtScalaVersionWithPrefix)
        val globalPluginUpdates = Reporter.globalPluginUpdates(
          sbtBinaryVersion.value,
          resolver, sbtVersionWithPrefix, sbtScalaVersionWithPrefix)
        val dependencyUpdates = Reporter.dependencyUpdates(
          scalaVersion.value, scalaBinaryVersion.value, libraryDependencies.value, resolver)
        bar.stop()
        import fansi._
        val style = Color.LightBlue ++ Reversed.On ++ Bold.On
        val projectId = thisProject.value.id
        val length = (100 - projectId.length) / 2
        val separator = style(wrap(projectId, " ", length))
        print(s"$separator \n")
        if (globalPluginUpdates.nonEmpty) {
          print(s"[info]  ${wrap("Global  Plugins", "-", 31)}\n")
          globalPluginUpdates.foreach(util.Logger.log)
        }
        if (pluginUpdates.nonEmpty) {
          print(s"[info]  ${wrap("Plugins", "-", 35)}\n")
          pluginUpdates.foreach(util.Logger.log)
        }
        print(s"[info] ${wrap("Dependencies", "-", 33)}\n")
        dependencyUpdates.foreach(util.Logger.log)
      })
  }

  private[sbt] def wrap(content: String, wrapWith: String, wrapLength: Int): String = {
    val range = 0 to wrapLength
    val wrapStr = range.map(_ ⇒ wrapWith).mkString
    s"$wrapStr $content $wrapStr"
  }

}
