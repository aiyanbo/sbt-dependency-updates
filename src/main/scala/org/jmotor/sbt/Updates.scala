package org.jmotor.sbt

import java.nio.file.{ Files, Path, Paths, StandardOpenOption }

import com.google.common.base.CaseFormat
import org.jmotor.sbt.dto.{ ModuleStatus, Status }
import sbt.ResolvedProject

import scala.collection.mutable.ListBuffer
import scala.io.Codec
import sbt.Keys.streams
import scalariform.formatter.ScalaFormatter

/**
 * Component:
 * Description:
 * Date: 2018/2/28
 *
 * @author AI
 */
object Updates {

  private[sbt] lazy val versionRegex = """val ?(\w+) ?= ?".*"""".r
  private[sbt] lazy val versionsObjectRegex = """[\t ]*object ?Versions ?\{([^{]*)[\t ]*\}""".r

  def applyUpdates(project: ResolvedProject, scalaVersion: String, updates: Seq[ModuleStatus]): Option[Int] = {
    getDependenciesPathOpt(project) map { path ⇒
      val text = new String(Files.readAllBytes(path), Codec.UTF8.charSet)
      val expiredModules = updates.collect {
        case m if m.status == Status.Expired ⇒
          val name = CaseFormat.LOWER_HYPHEN.to(CaseFormat.LOWER_CAMEL, m.module.name)
          name -> m.lastVersion
      }.toMap
      lazy val matchedNames = ListBuffer[String]()
      val versions = extractVersionLines(text).collect {
        case versionRegex(name) if expiredModules.contains(name) ⇒
          matchedNames += name
          s"""val $name = "${expiredModules(name)}""""
        case v ⇒ v
      }
      val appends = expiredModules.filterNot(v ⇒ matchedNames.contains(v._1)).map { v ⇒
        s"""val ${v._1} = "${v._2}""""
      }
      val _versions = (versions ++ appends).toSet.toSeq
      val newVersions = _versions.sortBy(_.length)
      val newText = text.replaceFirst(versionsObjectRegex.regex, s"object Versions {\n${newVersions.mkString("\n")}\n}")
      val result = ScalaFormatter.format(newText, scalaVersion = scalaVersion)
      Files.write(path, result.getBytes(Codec.UTF8.charSet), StandardOpenOption.WRITE)
      expiredModules.size
    }
  }

  private[sbt] def getDependenciesPathOpt(project: ResolvedProject): Option[Path] = {
    var path = Paths.get(project.base.getPath, "project", "Dependencies.scala")
    if (Files.exists(path)) {
      Option(path)
    } else {
      path = Paths.get(project.base.getParentFile.getPath, "project", "Dependencies.scala")
      Option(path).filter(p ⇒ Files.exists(p))
    }
  }

  private[sbt] def extractVersionLines(text: String): Array[String] = {
    (for (m ← versionsObjectRegex.findFirstMatchIn(text)) yield m.group(1)) match {
      case None ⇒ Array.empty
      case Some(v) ⇒
        v.split("\n").map { line ⇒
          line.replace("\t", "").trim
        }.filter(_.nonEmpty)
    }
  }

}
