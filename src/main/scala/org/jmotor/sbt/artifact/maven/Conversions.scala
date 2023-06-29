package org.jmotor.sbt.artifact.maven

import okhttp3.Request

import scala.language.implicitConversions

object Conversions {

  implicit class SearchRequestWrapper(request: SearchRequest) {

    implicit def toHttpRequest(root: String): Request.Builder =
      new Request.Builder().url(s"$root?${request.toParameter}")

  }

}
