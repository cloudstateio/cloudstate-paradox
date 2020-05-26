import sbt._
import sbt.Keys._
import bintray.{ BintrayKeys, BintrayPlugin }

object Publish extends AutoPlugin {
  import BintrayKeys._

  override def requires = BintrayPlugin
  override def trigger = allRequirements

  override def buildSettings = Seq(
    bintrayOrganization := Some("cloudstateio")
  )

  override def projectSettings = Seq(
    bintrayRepository := {
      val repository = if (isSnapshot.value) "snapshots" else "releases"
      if (sbtPlugin.value) s"sbt-plugin-$repository" else repository
    },
    bintrayPackage := normalizedName.value,
    licenses += "Apache-2.0" -> url("https://www.apache.org/licenses/LICENSE-2.0.html"),
    pomIncludeRepository := { _ => false },
    resourceGenerators in Compile += Def.task {
      val file = (resourceManaged in Compile).value / "cloudstate-paradox.properties"
      IO.write(file, s"cloudstate.paradox.version=${version.value}")
      Seq(file)
    }
  )
}

object NoPublish extends AutoPlugin {
  override def projectSettings = Seq(
    publish / skip := true
  )
}
