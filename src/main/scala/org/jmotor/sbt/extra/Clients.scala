package org.jmotor.sbt.extra

import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper}
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import okhttp3.{Call, Callback, OkHttpClient, Response}

import java.io.IOException
import scala.concurrent.{Future, Promise}

object Clients {

  lazy val okhttpClient: OkHttpClient = new OkHttpClient.Builder().build()

  lazy val jackson: ObjectMapper =
    JsonMapper
      .builder()
      .addModule(DefaultScalaModule)
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
      .build()

  implicit class OkHttpCallWrapper(call: Call) {

    implicit def toFuture: Future[Response] = {
      val promise = Promise[Response]
      call.enqueue(new Callback {
        override def onFailure(call: Call, e: IOException): Unit = promise.failure(e)

        override def onResponse(call: Call, response: Response): Unit =
          promise.success(response)
      })
      promise.future
    }

  }

  def withResponse[T](response: Response)(fn: => T): T =
    try {
      fn
    } finally {
      try {
        response.close()
      } catch {
        case _: Throwable => // ignore
      }
    }

  def collectResponse[T](response: Response)(fn: (Response) => T): T =
    withResponse(response) {
      fn(response)
    }

}
