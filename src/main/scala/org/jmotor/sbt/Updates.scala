package org.jmotor.sbt

import java.nio.file.{ Files, Path, Paths, StandardOpenOption }

import com.google.common.base.CaseFormat
import org.jmotor.sbt.dto.{ Component, ModuleStatus, Status }
import org.jmotor.sbt.parser.PluginParser
import org.jmotor.sbt.parser.VersionParser._
import org.jmotor.sbt.plugin.ComponentSorter
import org.jmotor.sbt.plugin.ComponentSorter.ComponentSorter
import sbt.ResolvedProject
import scalariform.formatter.ScalaFormatter

import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer
import scala.io.Codec
import scala.util.{ Failure, Success, Try }

/**
 * Component:
 * Description:
 * Date: 2018/2/28
 *
 * @author AI
 */
object Updates {

  def applyDependencyUpdates(project: ResolvedProject, scalaVersion: String,
                             updates: Seq[ModuleStatus], nameMappings: Map[String, String], sorter: ComponentSorter): Option[Int] = {
    getDependenciesPathOpt(project) map { path ⇒
      val text = new String(Files.readAllBytes(path), Codec.UTF8.charSet)
      val expiredModules = updates.collect {
        case m if m.status == Status.Expired ⇒
          val name = mappingModuleName(m.module.name, nameMappings)
          name -> m.lastVersion
      }.toMap
      lazy val matchedNames = ListBuffer[String]()
      val versions = parseVersionLines(text).collect {
        case v @ VersionRegex(name, version) ⇒
          expiredModules.find(_._1.equalsIgnoreCase(name)) match {
            case None ⇒ Component(name, version)
            case Some(component) ⇒
              matchedNames += component._1
              Component(component._1, component._2)
          }
      }

      val appends = expiredModules.filterNot(v ⇒ matchedNames.contains(v._1)).map { v ⇒
        Component(v._1, v._2)
      }
      val components = sortComponents((versions ++ appends).toSet.toSeq, sorter)
      val componentLines = components.map(c ⇒ s"""val ${c.name} = "${c.version}"""")
      val newText = text.replaceFirst(VersionsObjectRegex.regex, s"object Versions {\n${componentLines.mkString("\n")}\n}")
      val result = ScalaFormatter.format(newText, scalaVersion = scalaVersion)
      Files.write(path, result.getBytes(Codec.UTF8.charSet), StandardOpenOption.TRUNCATE_EXISTING)
      expiredModules.size
    }
  }

  def applyPluginUpdates(project: ResolvedProject, scalaVersion: String, updates: Seq[ModuleStatus]): Int = {
    val expiredModules = updates.filter(_.status == Status.Expired)
    getPluginSources(project).foreach {
      case (path, lines) ⇒
        val text = lines.map {
          case line if line.startsWith("addSbtPlugin") ⇒
            line match {
              case PluginParser.AddSbtPluginRegex(org, n, v) ⇒
                val module = expiredModules.find(s ⇒ s.module.organization == org && s.module.name == n)
                module.map(s ⇒ line.replace(v, s.lastVersion)).getOrElse(line)
              case _ ⇒ line
            }
          case line ⇒ line
        }.mkString("\n") + "\n"
        Files.write(path, text.getBytes(Codec.UTF8.charSet), StandardOpenOption.TRUNCATE_EXISTING)
    }
    expiredModules.size
  }

  private[sbt] def sortComponents(components: Seq[Component], sorter: ComponentSorter): Seq[Component] = {
    sorter match {
      case ComponentSorter.ByAlphabetically ⇒ components.sortBy(_.name)
      case _                                ⇒ components.sortBy(c ⇒ (c.name + c.version).length)
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

  private[sbt] def getPluginSources(project: ResolvedProject): Seq[(Path, Seq[String])] = {
    Try {
      val dir = Paths.get(project.base.getPath, "project")
      Files.newDirectoryStream(dir, "*.sbt").asScala.toSeq.map { path ⇒
        path -> Files.readAllLines(path).asScala
      }
    } match {
      case Success(sources) ⇒ sources
      case Failure(_)       ⇒ Seq.empty
    }
  }

  private[sbt] def mappingModuleName(moduleName: String, nameMappings: Map[String, String]): String = {
    val nameMapping = nameMappings.get(moduleName) orElse nameMappings.collectFirst {
      case (k, v) if moduleName.matches(k) ⇒ v
    }
    nameMapping.getOrElse {
      CaseFormat.LOWER_HYPHEN.to(CaseFormat.LOWER_CAMEL, moduleName)
    }
  }

}
