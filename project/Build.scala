import sbt._
import Keys._
import play.Project._
import org.jba.sbt.plugin.MustachePlugin
import org.jba.sbt.plugin.MustachePlugin._

object ApplicationBuild extends Build {

  val appName         = "loudcloud"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    "org.jba" %% "play2-mustache" % "1.1.2"
  )

  val main = play.Project(appName, appVersion, appDependencies).settings(
    resolvers += Resolver.url("julienba.github.com", url("http://julienba.github.com/repo/"))(Resolver.ivyStylePatterns),
    lessEntryPoints <<= baseDirectory(_ / "app" / "assets" / "stylesheets" ** "loudcloud.less"),
    requireJs += "room.js",
    // Mustache settings
    mustacheEntryPoints <<= (sourceDirectory in Compile)(base => base / "assets" / "mustache" ** "*.html"),
    mustacheOptions := Seq.empty[String],
    resourceGenerators in Compile <+= MustacheFileCompiler
  )

}
