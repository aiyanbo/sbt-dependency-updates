package org.jmotor.sbt

import java.nio.file.{ Files, Path, Paths, StandardOpenOption }

import com.google.common.base.CaseFormat
import org.jmotor.sbt.dto.{ ModuleStatus, Status }
import org.jmotor.sbt.parser.VersionParser._
import sbt.ResolvedProject

import scala.collection.mutable.ListBuffer
import scala.io.Codec

import scalariform.formatter.ScalaFormatter

/**
 * Component:
 * Description:
 * Date: 2018/2/28
 *
 * @author AI
 */
object Updates {

  def applyUpdates(project: ResolvedProject, scalaVersion: String, updates: Seq[ModuleStatus]): Option[Int] = {
    getDependenciesPathOpt(project) map { path ⇒
      val text = new String(Files.readAllBytes(path), Codec.UTF8.charSet)
      val expiredModules = updates.collect {
        case m if m.status == Status.Expired ⇒
          val name = CaseFormat.LOWER_HYPHEN.to(CaseFormat.LOWER_CAMEL, m.module.name)
          name -> m.lastVersion
      }.toMap
      lazy val matchedNames = ListBuffer[String]()
      val versions = parseVersionLines(text).map {
        case v @ VersionRegex(name) ⇒
          expiredModules.find(_._1.equalsIgnoreCase(name)) match {
            case None ⇒ v
            case Some(version) ⇒
              matchedNames += version._1
              s"""val ${version._1} = "${version._2}""""
          }
        case v ⇒ v
      }
      val appends = expiredModules.filterNot(v ⇒ matchedNames.contains(v._1)).map { v ⇒
        s"""val ${v._1} = "${v._2}""""
      }
      val _versions = (versions ++ appends).toSet.toSeq
      val newVersions = _versions.sortBy(_.length)
      val newText = text.replaceFirst(VersionsObjectRegex.regex, s"object Versions {\n${newVersions.mkString("\n")}\n}")
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

}
