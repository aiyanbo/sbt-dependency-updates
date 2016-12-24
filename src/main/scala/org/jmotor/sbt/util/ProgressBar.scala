package org.jmotor.sbt.util

import java.util.concurrent.atomic.AtomicInteger

/**
 * Component:
 * Description:
 * Date: 2016/12/24
 *
 * @author AI
 */
class ProgressBar(message: String, done: String) {
  private[this] var running: Boolean = true
  private[this] val chars = Seq("/", "-", "\\", "|")
  private[this] val worker = new Thread() {
    private[this] val index = new AtomicInteger(0)

    override def run(): Unit = {
      while (running) {
        var _index = index.getAndIncrement()
        if (_index >= chars.length) {
          _index = 0
          index.set(0)
        }
        print(s"$message ${chars(_index)} \r")
        Thread.sleep(200)
      }
    }
  }

  def start(): Unit = worker.start()

  def stop(): Unit = {
    running = false
    print(s"$done\n")
  }

}
