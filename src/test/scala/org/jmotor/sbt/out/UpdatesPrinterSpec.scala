package org.jmotor.sbt.out

import org.jmotor.sbt.out.UpdatesPrinter.wrap
import org.scalatest.funsuite.AnyFunSuite

/** Component: Description: Date: 2018/2/26
  *
  * @author
  *   AI
  */
class UpdatesPrinterSpec extends AnyFunSuite {

  test("print layout") {
    val width = 80
    val t1 = s"[info] ${wrap("Global Plugins", "-", width)}"
    val t2 = s"[info] ${wrap(" Plugins", "-", width)}"
    val t3 = s"[info] ${wrap("Dependencies", "-", width)}"
    assert(t1.length == t2.length && t2.length == t3.length)
  }

}
