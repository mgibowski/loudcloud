import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "loudcloud"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq()

  val main = play.Project(appName, appVersion, appDependencies).settings(
    lessEntryPoints <<= baseDirectory(_ / "app" / "assets" / "stylesheets" ** "loudcloud.less"),
    requireJs += "room.js"
  )

}
