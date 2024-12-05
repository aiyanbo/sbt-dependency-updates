package org.jmotor.sbt.service

import org.jmotor.sbt.dto.ModuleStatus
import sbt.Credentials
import sbt.librarymanagement.{ModuleID, Resolver}
import sbt.util.Logger

import scala.concurrent.Future

/**
 * Component: Description: Date: 2018/2/9
 *
 * @author
 *   AI
 */
trait VersionService {

  def checkForUpdates(module: ModuleID): Future[ModuleStatus]

  def checkPluginForUpdates(
    module: ModuleID,
    sbtBinaryVersion: String,
    sbtScalaBinaryVersion: String
  ): Future[ModuleStatus]

}

object VersionService {

  def apply(
    logger: Logger,
    scalaVersion: String,
    scalaBinaryVersion: String,
    resolvers: Seq[Resolver],
    credentials: Seq[Credentials]
  ): VersionService =
    new VersionServiceImpl(logger, scalaVersion, scalaBinaryVersion, resolvers, credentials)

}
