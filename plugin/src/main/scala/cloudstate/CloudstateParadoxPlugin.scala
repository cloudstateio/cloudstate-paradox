package cloudstate

import sbt._
import sbt.Keys._
import com.lightbend.paradox.sbt.ParadoxPlugin

object CloudstateParadoxPlugin extends AutoPlugin {
  import ParadoxPlugin.autoImport._

  override def requires = ParadoxPlugin
  override def trigger = noTrigger

  val version = ParadoxPlugin.readProperty("cloudstate-paradox.properties", "cloudstate.paradox.version")

  override def projectSettings: Seq[Setting[_]] = Seq(
    resolvers += Resolver.bintrayRepo("cloudstateio", "releases"),
    paradoxTheme := Some("io.cloudstate" % "cloudstate-paradox-theme" % version),
    paradoxNavigationDepth := 3
  )
}
