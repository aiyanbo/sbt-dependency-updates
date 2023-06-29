package org.jmotor.sbt.artifact.exception

/**
 * Component: Description: Date: 2018/2/15
 *
 * @author
 *   AI
 */
final case class ArtifactNotFoundException(organization: String, artifactId: String)
    extends RuntimeException(s"organization: $organization, artifactId: $artifactId not found")
