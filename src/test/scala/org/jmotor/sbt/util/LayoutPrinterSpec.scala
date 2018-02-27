package org.jmotor.sbt.util

import org.jmotor.sbt.util.LayoutPrinter.wrap
import org.scalatest.FunSuite

/**
  * Component:
  * Description:
  * Date: 2018/2/26
  *
  * @author AI
  */
class LayoutPrinterSpec extends FunSuite {

  test("print layout") {
    val width = 80
    val t1 = s"[info] ${wrap("Global Plugins", "-", width)}"
    val t2 = s"[info] ${wrap(" Plugins", "-", width)}"
    val t3 = s"[info] ${wrap("Dependencies", "-", width)}"
    assert(t1.length == t2.length && t2.length == t3.length)
  }


}
