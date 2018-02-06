import Settings.Formatting

organization := "org.jmotor.sbt"

name := "sbt-dependency-updates"

version := "1.1.1"

sbtPlugin := true

libraryDependencies ++= Seq(
  "com.lihaoyi" %% "fansi" % "0.2.3",
  "org.slf4j" % "slf4j-simple" % "1.7.21",
  "org.apache.httpcomponents" % "httpclient" % "4.5.3",
  "org.scalatest" %% "scalatest" % "3.0.3" % "test"
)

crossSbtVersions := Seq("1.0.3", "0.13.16")

publishMavenStyle := true

scalacOptions ++= Seq("-deprecation", "-unchecked")

Formatting.formatSettings

credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value) {
    Some("snapshots" at nexus + "content/repositories/snapshots")
  }
  else {
    Some("releases" at nexus + "service/local/staging/deploy/maven2")
  }
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
