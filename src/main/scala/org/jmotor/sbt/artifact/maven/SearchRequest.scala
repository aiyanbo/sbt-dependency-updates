package org.jmotor.sbt.artifact.maven

import okhttp3.Credentials

case class SearchRequest(
  groupId: Option[String],
  artifactId: Option[String],
  version: Option[String],
  tags: Option[String] = None,
  delimiter: String = " AND ",
  rows: Int = 20,
  start: Int = 0,
  core: String = "gav",
  wt: String = "json",
  credentials: Option[Credentials] = None
) {
  def toParameter: String = {
    val parameters: String = List(
      groupId.map(v => s"""g:"$v"""").getOrElse(""),
      artifactId.map(v => s"""a:"$v"""").getOrElse(""),
      version.map(v => s"""v:"$v"""").getOrElse(""),
      tags.map(v => s"""tags:"$v"""").getOrElse("")
    ).filter(v => v.nonEmpty).reduce(_ + delimiter + _)
    s"q=$parameters&core=$core&rows=$rows&wt=$wt&start=$start"
  }
}

object SearchRequest {
  def apply(groupId: String, artifactId: String): SearchRequest =
    new SearchRequest(Option(groupId), Option(artifactId), None)

}
