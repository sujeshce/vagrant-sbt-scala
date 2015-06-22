import sbt._
import Keys._

import Vagrant._

object build extends Build {

  lazy val root = Project (
    id = "vagrant-tests",
    base = file("."),
    settings = Defaults.defaultSettings ++ Vagrant.settings
  )
}