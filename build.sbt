import Settings.Formatting

scalaVersion := "2.10.6"

organization := "org.jmotor.sbt"

name := "sbt-dependency-updates"

version := "1.0.0"

sbtPlugin := true

libraryDependencies ++= Seq(
  "com.lihaoyi" %% "fansi" % "0.2.3",
  "org.asynchttpclient" % "async-http-client" % "2.0.24"
)

publishMavenStyle := true

scalacOptions ++= Seq("-deprecation", "-unchecked")

Formatting.formatSettings

credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

pomExtra :=
  <url>https://github.com/aiyanbo/sbt-dependency-updates</url>
    <licenses>
      <license>
        <name>Apache License</name>
        <url>http://www.apache.org/licenses/</url>
        <distribution>repo</distribution>
      </license>
    </licenses>
    <scm>
      <url>git@github.com:aiyanbo/sbt-dependency-updates.git</url>
      <connection>scm:git:git@github.com:aiyanbo/sbt-dependency-updates.git</connection>
    </scm>
    <developers>
      <developer>
        <id>yanbo.ai</id>
        <name>Andy Ai</name>
        <url>http://aiyanbo.github.io/</url>
      </developer>
    </developers>
