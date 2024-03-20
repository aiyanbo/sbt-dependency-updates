package org.jmotor.sbt.out

import org.jmotor.sbt.dto.{ModuleStatus, Status}
import org.jmotor.sbt.out.UpdatesPrinter.*
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import sbt.*

import java.io.{ByteArrayOutputStream, PrintStream}
import java.nio.charset.StandardCharsets

/**
 * Component: Description: Date: 2018/2/26
 *
 * @author
 *   AI
 */
class UpdatesPrinterSpec extends AnyFunSuite with Matchers {

  test("print report") {
    val consoleOut = captureConsoleOut(
      printReporter(
        "mainModule",
        Seq(
          ModuleStatus("oraganization" % "name" % "0.0.1", Status.Success),
          ModuleStatus("oraganization" % "name" % "0.0.1", Status.Unreleased, "1.0.0")
        ),
        Seq(ModuleStatus("oraganization" % "name" % "0.0.1", Status.Expired, "1.0.0")),
        Seq(ModuleStatus("oraganization" % "name" % "0.0.1", Status.Unreleased, "1.0.0"))
      )
    )

    consoleOut.uncolored shouldBe
      s"""|                                             mainModule${"                                             "}
          |[info] -------------------------------- Global Plugins --------------------------------
          |[expired   ] oraganization:name:0.0.1 ---> 1.0.0
          |[info] ----------------------------------- Plugins ------------------------------------
          |[success   ] oraganization:name:0.0.1 √
          |[unreleased] oraganization:name:0.0.1 ---> 1.0.0
          |[info] --------------------------------- Dependencies ---------------------------------
          |[unreleased] oraganization:name:0.0.1 ---> 1.0.0
          |""".stripMargin
  }

  test("print layout") {
    val width = 80
    val t1    = s"[info] ${wrap("Global Plugins", "-", width)}"
    val t2    = s"[info] ${wrap("Plugins", "-", width)}"
    val t3    = s"[info] ${wrap("Dependencies", "-", width)}"
    assert(t1.length == t2.length && t2.length == t3.length)
  }

  test("status line") {
    val t1 = statusLine(ModuleStatus("oraganization" % "name" % "0.0.1", Status.Success))
    val t2 = statusLine(ModuleStatus("oraganization" % "name" % "0.0.1", Status.Expired, "1.0.0"))
    val t3 = statusLine(ModuleStatus("oraganization" % "name" % "0.0.1", Status.Error))
    val t4 = statusLine(ModuleStatus("oraganization" % "name" % "0.0.1", Status.Unreleased, "1.0.0"))

    t1.uncolored shouldBe "[success   ] oraganization:name:0.0.1 √"
    t2.uncolored shouldBe "[expired   ] oraganization:name:0.0.1 ---> 1.0.0"
    t3.uncolored shouldBe "[error     ] oraganization:name:0.0.1 updates error, please retry!"
    t4.uncolored shouldBe "[unreleased] oraganization:name:0.0.1 ---> 1.0.0"
  }

  def captureConsoleOut(f: => Any): String = {
    val outBuffer          = new ByteArrayOutputStream()
    val interceptionStream = new PrintStream(outBuffer, false, StandardCharsets.UTF_8)

    scala.Console.withOut(interceptionStream)(f)

    val output = new String(outBuffer.toByteArray, StandardCharsets.UTF_8)
    output
  }

  implicit class StringImplicits(s: String) {
    def uncolored: String = s.replaceAll("\u001B\\[[;\\d]*m", "")
  }

}
