package org.jmotor.sbt

package object scalafmt {

  val conf: String =
    """
      |version = "3.5.8"
      |maxColumn = 120
      |runner.dialect = scala213
      |""".stripMargin

}
