package org.jmotor.sbt.artifact.maven

case class Artifact(id: String, g: String, a: String, v: String, timestamp: Long, tags: Seq[String])
