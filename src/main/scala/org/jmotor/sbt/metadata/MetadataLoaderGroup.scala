package org.jmotor.sbt.metadata

import org.apache.maven.artifact.versioning.ArtifactVersion
import org.jmotor.sbt.artifact.exception.ArtifactNotFoundException
import org.jmotor.sbt.artifact.metadata.MetadataLoader
import org.jmotor.sbt.artifact.metadata.loader.IvyPatternsMetadataLoader
import org.jmotor.sbt.concurrent.MultiFuture
import sbt.librarymanagement.{Binary, Constant, Disabled, Full, ModuleID, Patch}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.{Failure, Success}

/**
 * Component: Description: Date: 2018/3/1
 *
 * @author
 *   AI
 */
class MetadataLoaderGroup(scalaVersion: String, scalaBinaryVersion: String, loaders: Seq[MetadataLoader]) {

  def getVersions(module: ModuleID, sbtSettings: Option[(String, String)]): Future[Seq[ArtifactVersion]] =
    if (loaders.lengthCompare(1) > 0) {
      firstCompletedOf(loaders.map { loader =>
        val (artifactId, attrs) = getArtifactIdAndAttrs(loader, module, sbtSettings)
        loader.getVersions(module.organization, artifactId, attrs)
      })
    } else {
      loaders.headOption.fold(Future.successful(Seq.empty[ArtifactVersion])) { loader =>
        val (artifactId, attrs) = getArtifactIdAndAttrs(loader, module, sbtSettings)
        loader.getVersions(module.organization, artifactId, attrs)
      }
    }

  private[metadata] def firstCompletedOf(
    futures: TraversableOnce[Future[Seq[ArtifactVersion]]]
  )(implicit executor: ExecutionContext): Future[Seq[ArtifactVersion]] = {
    val p           = Promise[Seq[ArtifactVersion]]()
    val multiFuture = new MultiFuture[Seq[ArtifactVersion]](p, futures.size, Seq.empty)
    futures foreach { future =>
      future.onComplete {
        case Success(r) if r.nonEmpty              => p trySuccess r
        case Success(_)                            => multiFuture.tryComplete()
        case Failure(_: ArtifactNotFoundException) => multiFuture.tryComplete()
        case Failure(t)                            => multiFuture.tryComplete(t)
      }(scala.concurrent.ExecutionContext.Implicits.global)
    }
    p.future
  }

  private[metadata] def getArtifactIdAndAttrs(
    loader: MetadataLoader,
    module: ModuleID,
    sbtSettings: Option[(String, String)]
  ): (String, Map[String, String]) = {
    val remapVersion = module.crossVersion match {
      case _: Disabled        => sbtSettings.map { case (sbt, scala) => s"${scala}_$sbt" }
      case binary: Binary     => Option(binary.prefix + scalaBinaryVersion)
      case _: Full            => Option(scalaVersion)
      case _: Patch           => Option(scalaVersion)
      case constant: Constant => Option(constant.value)
      case _                  => None
    }
    val name = remapVersion.map(v => s"${module.name}_$v").getOrElse(module.name)
    loader match {
      case _: IvyPatternsMetadataLoader if sbtSettings.isDefined =>
        val settings = sbtSettings.get
        name -> Map("sbtVersion" -> settings._1, "scalaVersion" -> settings._2)
      case _ => name -> Map.empty
    }
  }

}

object MetadataLoaderGroup {

  def apply(scalaVersion: String, scalaBinaryVersion: String, loaders: MetadataLoader*): MetadataLoaderGroup =
    new MetadataLoaderGroup(scalaVersion, scalaBinaryVersion, loaders)

}
