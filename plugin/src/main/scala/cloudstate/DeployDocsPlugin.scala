package cloudstate

import sbt._
import sbt.Keys._
import sbt.util.CacheStoreFactory
import scala.sys.process.{ Process, ProcessLogger }

object DeployDocsPlugin extends AutoPlugin {

  object DeployDocsKeys {
    val deployRepository = settingKey[String]("Github Pages repository to deploy docs")
    val deployBranch = settingKey[String]("Github Pages branch to deploy docs")
    val deployTokenVar = settingKey[String]("Environment variable name for github token (with repo access)")
    val deployName = settingKey[String]("Author name for deploy commit")
    val deployEmail = settingKey[String]("Author email for deploy commit")
    val deployModule = settingKey[String]("Name of the docs module (example: 'core', 'java', or 'go')")
    val deployModuleRequired = settingKey[Boolean]("Whether a docs module is required (default: true)")
    val deployVersion = settingKey[String]("Version of deployed docs (example: '1.2.3')")
    val deployAliases = settingKey[Seq[String]]("Aliases to create for this version (example: 'current')")
    val deployMappings = taskKey[Seq[(File, String)]]("File-to-path mappings for files to deploy")
    val deploy = taskKey[Unit]("Deploy the docs to Github Pages")
  }

  override def requires = plugins.JvmPlugin
  override def trigger = noTrigger

  val autoImport = DeployDocsKeys

  import autoImport._

  override def projectSettings: Seq[Setting[_]] = Seq(
    deployRepository := "cloudstateio/docs",
    deployBranch := "master",
    deployTokenVar := "DEPLOY_DOCS_TOKEN",
    deployName := "Cloudstate Bot",
    deployEmail := "deploy@cloudstate.io",
    deployModuleRequired := true,
    deployVersion := { if (isSnapshot.value) "snapshot" else version.value },
    deployAliases := { if (isSnapshot.value) Seq.empty else Seq("current") },
    deployMappings := Seq.empty,
    deploy := deployGithubPages(
      deployRepository.value,
      deployBranch.value,
      deployTokenVar.value,
      deployName.value,
      deployEmail.value,
      deployModule.?.value,
      deployModuleRequired.value,
      deployVersion.value,
      deployAliases.value,
      deployMappings.value,
      target.value,
      streams.value.cacheStoreFactory,
      streams.value.log
    )
  )

  def deployGithubPages(
    repo: String,
    branch: String,
    tokenVar: String,
    author: String,
    email: String,
    module: Option[String],
    moduleRequired: Boolean,
    version: String,
    aliases: Seq[String],
    mappings: Seq[(File, String)],
    target: File,
    cacheStoreFactory: CacheStoreFactory,
    log: Logger
  ): Unit = {
    if (module.isEmpty && moduleRequired)
      sys.error("""Deploy module is required. Set with `deployModule := "module"`.""")

    val targetDir = target / "deploy"
    val docsDir = targetDir / "docs"
    val moduleDir = module.fold(docsDir)(name => docsDir / name)
    val versionDir = moduleDir / version
    val versionPath = versionDir.relativeTo(docsDir).getOrElse(version)
    val versions = versionPath + (if (aliases.nonEmpty) aliases.mkString(" (", ", ", ")") else "")
    val token = sys.env.get(tokenVar)
    val repoUrl = token.fold(s"git@github.com:${repo}.git")(token => s"https://${token}@github.com/${repo}.git")

    def info(message: String): Unit = log.info(s"[${scala.Console.BLUE}deploy${scala.Console.RESET}] $message")

    info(s"Deploying $repo $versions ...")

    targetDir.mkdirs()
    if (docsDir.exists) IO.delete(docsDir)
    info(s"Cloning repository to $docsDir")
    cmd(targetDir, "git", "clone", "--branch", branch, "--depth=1", repoUrl, docsDir.absolutePath)

    val mapped = mappings.map { case (file, path) => (file, versionDir / path) }
    info(s"Syncing docs to $versionPath")
    Sync.sync(cacheStoreFactory.make("docs-sync"))(mapped)
    cmd(docsDir, "git", "add", "--all", versionDir.absolutePath)

    for (alias <- aliases) {
      val link = moduleDir / alias
      val linkPath = link.relativeTo(docsDir).getOrElse(link.name)
      info(s"Aliasing $linkPath -> $versionPath")
      cmd(docsDir, "ln", "-nsf", version, link.absolutePath)
      cmd(docsDir, "git", "add", link.absolutePath)
    }

    if (cmd(docsDir, "git", "status", "--untracked-files=no", "--porcelain").nonEmpty) {
      info(s"Pushing changes to $repo:$branch")
      cmd(docsDir, "git", "config", "user.name", author)
      cmd(docsDir, "git", "config", "user.email", email)
      cmd(docsDir, "git", "commit", "-m", s"Deploy $repo $versions")
      cmd(docsDir, "git", "push", repoUrl, branch)
      cmd(docsDir, "git", "show", "--stat-count=10", "HEAD").lines.foreach(info)
    } else {
      info(s"No changes to deploy for $repo")
    }
  }

  def cmd(workingDir: File, command: String*): String =
    Process(command, workingDir).!!(NoProcessLogger).trim

  object NoProcessLogger extends ProcessLogger {
    override def out(s: => String): Unit = ()
    override def err(s: => String): Unit = ()
    override def buffer[T](f: => T): T = f
  }
}
