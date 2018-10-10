import ReleaseTransformations._

sbtPlugin := true

organization := "org.jmotor.sbt"

name := "sbt-dependency-updates"

libraryDependencies ++= Dependencies.dependencies

releasePublishArtifactsAction := PgpKeys.publishSigned.value

releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,                               
  runTest,                                
  setReleaseVersion,                      
  commitReleaseVersion,
  tagRelease,                             
  publishArtifacts,
  setNextVersion,                         
  commitNextVersion,                      
  pushChanges
)
