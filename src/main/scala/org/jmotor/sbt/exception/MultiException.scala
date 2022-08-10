package org.jmotor.sbt.exception

/**
 * Component: Description: Date: 2018/3/1
 *
 * @author
 *   AI
 */
case class MultiException(exceptions: Throwable*) extends RuntimeException {

  def getMessages: Seq[String] = exceptions.map { e =>
    if (Option(e.getLocalizedMessage).isDefined) e.getLocalizedMessage else e.getClass.getName
  }

}
