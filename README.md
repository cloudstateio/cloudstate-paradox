# Cloudstate Paradox

**Deprecated: Cloudstate documentation is now using Antora. See [cloudstate-antora](https://github.com/cloudstateio/cloudstate-antora).**

This is a shared paradox theme and plugin for the Cloudstate family of projects.


## Usage

Add the sbt plugin for Cloudstate Paradox (in `plugins.sbt`):

```scala
addSbtPlugin("io.cloudstate" % "sbt-cloudstate-paradox" % "<version>")
```

Enable the plugin (instead of the upstream `ParadoxPlugin`):

```scala
enablePlugins(CloudstateParadoxPlugin)
```

For more settings refer to the [Paradox documentation](https://developer.lightbend.com/docs/paradox/latest/).
