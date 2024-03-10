package org.jmotor.sbt.service

import org.jmotor.sbt.dto.Status
import org.scalatest.funsuite.AnyFunSuite
import sbt.librarymanagement.{Binary, MavenRepo, ModuleID, Resolver}
import sbt.util.Logger

import scala.concurrent.Await
import scala.concurrent.duration.*

/** Component: Description: Date: 2018/3/1
 *
 * @author
 * AI
 */
class VersionServiceSpec extends AnyFunSuite {

  private[this] val resolvers = Seq(
    MavenRepo(
      "m2",
      "https://repo1.maven.org/maven2/"
    ),
    Resolver.sbtPluginRepo("releases")
  )

  test("check normal module") {
    val versionService =
      VersionService(Logger.Null, "2.12.4", "2.12", resolvers, Seq.empty)
    val future = versionService.checkForUpdates(
      ModuleID("com.google.guava", "guava", "23.0-jre")
    )
    val status = Await.result(future, 30.seconds)
    assert(status.status == Status.Expired)
  }

  test("check sbt plugin") {
    val versionService =
      VersionService(Logger.Null, "2.12.4", "2.12", resolvers, Seq.empty)
    val future = versionService.checkPluginForUpdates(
      ModuleID("org.jetbrains", "sbt-idea-shell", "2017.2"),
      "1.0",
      "2.12"
    )
    val status = Await.result(future, 30.seconds)
    assert(status.status == Status.Expired)
  }

  test("check scalajs dependency") {
    val versionService =
      VersionService(Logger.Null, "2.12.4", "2.12", resolvers, Seq.empty)
    val future = versionService.checkPluginForUpdates(
      ModuleID("org.scala-js", "scalajs-dom", "1.2.0").withCrossVersion(Binary("sjs1_", "")),
      "1.0",
      "2.12"
    )
    val status = Await.result(future, 30.seconds)
    assert(status.status == Status.Expired)
  }

}
