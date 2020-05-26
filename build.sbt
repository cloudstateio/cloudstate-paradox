lazy val `cloudstate-paradox` = project
  .in(file("."))
  .enablePlugins(NoPublish)
  .aggregate(theme, plugin)

lazy val theme = project
  .in(file("theme"))
  .enablePlugins(ParadoxThemePlugin)
  .settings(
    organization := "io.cloudstate",
    name := "cloudstate-paradox-theme",
    libraryDependencies ++= Seq(
      Library.foundation % Provided,
      Library.prettify % Provided
    )
  )

lazy val plugin = project
  .in(file("plugin"))
  .enablePlugins(SbtPlugin)
  .settings(
    sbtPlugin := true,
    organization := "io.cloudstate",
    name := "sbt-cloudstate-paradox",
    addSbtPlugin(Library.sbtParadox)
  )
