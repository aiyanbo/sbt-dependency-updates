package org.jmotor.sbt.service

import org.jmotor.sbt.dto.ModuleStatus
import sbt.Credentials
import sbt.librarymanagement.{ ModuleID, Resolver }

/**
 * Component:
 * Description:
 * Date: 2018/2/9
 *
 * @author AI
 */
trait VersionService {

  def checkForUpdates(module: ModuleID): ModuleStatus

  def checkPluginForUpdates(module: ModuleID, sbtVersion: String, scalaVersion: String): ModuleStatus

}

object VersionService {

  def apply(resolvers: Seq[Resolver], credentials: Seq[Credentials]): VersionService = {
    new VersionServiceImpl(resolvers, credentials)
  }

}
