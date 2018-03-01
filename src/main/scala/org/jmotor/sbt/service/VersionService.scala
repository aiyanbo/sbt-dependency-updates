package org.jmotor.sbt.service

import org.jmotor.sbt.dto.ModuleStatus
import sbt.Credentials
import sbt.librarymanagement.{ ModuleID, Resolver }

import scala.concurrent.Future

/**
 * Component:
 * Description:
 * Date: 2018/2/9
 *
 * @author AI
 */
trait VersionService {

  def checkForUpdates(module: ModuleID): Future[ModuleStatus]

  def checkPluginForUpdates(module: ModuleID, sbtBinaryVersion: String, sbtScalaBinaryVersion: String): Future[ModuleStatus]

}

object VersionService {

  def apply(scalaVersion: String, scalaBinaryVersion: String,
            resolvers: Seq[Resolver], credentials: Seq[Credentials]): VersionService = {
    new VersionServiceImpl(scalaVersion, scalaBinaryVersion, resolvers, credentials)
  }

}
