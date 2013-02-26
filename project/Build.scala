import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "play-modules"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    "org.pegdown" % "pegdown" % "1.2.1"
  )


  val main = play.Project(appName, appVersion, appDependencies)

}
