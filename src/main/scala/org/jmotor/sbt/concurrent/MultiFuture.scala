package org.jmotor.sbt.concurrent

import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicInteger

import org.jmotor.sbt.exception.MultiException

import scala.concurrent.Promise

/**
 * Component:
 * Description:
 * Date: 2018/3/1
 *
 * @author AI
 */
class MultiFuture[T](p: Promise[T], count: Int, default: T) {
  private[this] val counter = new AtomicInteger(0)
  private[this] val errors = new CopyOnWriteArrayList[Throwable]()

  def tryComplete(): Unit = {
    if (counter.incrementAndGet() == count) {
      if (errors.isEmpty) {
        p success default
      } else {
        import scala.collection.JavaConverters._
        p failure MultiException(errors.asScala: _*)
      }
    }
  }

  def tryComplete(throwable: Throwable): Unit = {
    errors.add(throwable)
    tryComplete()
  }

}
