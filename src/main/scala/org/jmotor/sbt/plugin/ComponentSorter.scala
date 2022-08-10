package org.jmotor.sbt.plugin

/**
 * @author
 *   AI 2019-04-26
 */
object ComponentSorter extends Enumeration {

  type ComponentSorter = Value

  val ByLength: Value = Value(0)

  val ByAlphabetically: Value = Value(1)

}
