package sbtmod

import sbt.Credentials
import sbt.librarymanagement.ModuleID
import sbt.librarymanagement.Resolver

/**
 * @author
 *   AI 2020/3/17
 */
class Mod {

  def checkUpdates(
    dependency: ModuleID,
    scalaVersion: String,
    scalaBinaryVersion: String,
    resolvers: Seq[Resolver],
    credentials: Seq[Credentials]
  ): ModStatus =
    None.orNull

}
