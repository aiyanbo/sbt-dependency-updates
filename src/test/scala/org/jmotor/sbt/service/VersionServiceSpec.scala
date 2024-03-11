package org.jmotor.sbt.service

import org.jmotor.sbt.dto.Status
import org.scalatest.funsuite.AnyFunSuite
import sbt.librarymanagement.{Binary, MavenRepo, ModuleID, Resolver}
import sbt.util.Logger

import scala.concurrent.Await
import scala.concurrent.duration.*
import java.net.{URI, URL, URLConnection, URLStreamHandler, URLStreamHandlerFactory}
import java.util.concurrent.atomic.AtomicReference

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

  test("uses custom protocol handlers") {
    val downloadsCalled = new AtomicReference(Vector.empty[String])
      
    // setting stream handler can be executed only once on jvm
    // to be able to execute it multiple times from sbt shell it requires forking enabled like "Test / fork := true"
    URL.setURLStreamHandlerFactory(new URLStreamHandlerFactory {
      def createURLStreamHandler(protocol: String): URLStreamHandler = protocol match {
        case "artifactregistry" =>
          new URLStreamHandler {
            protected def openConnection(url: URL): URLConnection = {
              downloadsCalled.getAndUpdate(_ :+ url.toString())
              new URI(s"https://${url.getHost}${url.getPath()}").normalize.toURL.openConnection
            }
          }
        case _ => null
      }
    })

    val testResolver   = MavenRepo("m2", "artifactregistry://repo1.maven.org/maven2/")
    val versionService = VersionService(Logger.Null, "2.12.4", "2.12", Seq(testResolver), Seq.empty)
    Await.result(versionService.checkForUpdates(ModuleID("com.google.guava", "guava", "23.0-jre")), 30.seconds)

    assert(
      downloadsCalled
        .get()
        .contains("artifactregistry://repo1.maven.org/maven2/com/google/guava/guava/maven-metadata.xml")
    )
  }

}
