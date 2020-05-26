import sbt._

object Version {
  val foundation = "6.2.4"
  val paradox    = "0.8.0"
  val prettify   = "4-Mar-2013-1"
}

object Library {
  val foundation = "org.webjars"           % "foundation"  % Version.foundation
  val prettify   = "org.webjars"           % "prettify"    % Version.prettify
  val sbtParadox = "com.lightbend.paradox" % "sbt-paradox" % Version.paradox
}
