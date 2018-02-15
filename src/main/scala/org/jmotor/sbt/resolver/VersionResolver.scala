package org.jmotor.sbt.resolver

import org.jmotor.sbt.model.ModuleStatus
import sbt.librarymanagement.{ ModuleID, Resolver }
import sbt.Credentials

/**
 * Component:
 * Description:
 * Date: 2018/2/9
 *
 * @author AI
 */
trait VersionResolver {

  def checkForUpdates(module: ModuleID): ModuleStatus

  def checkPluginForUpdates(module: ModuleID, sbtVersion: String, scalaVersion: String): ModuleStatus

}

object VersionResolver {

  def apply(resolvers: Seq[Resolver], credentials: Seq[Credentials]): VersionResolver = {
    new VersionResolverImpl(resolvers, credentials)
  }

}
