import sbt._
import sbt.Keys._

sealed trait VagrantStatus
case object Running extends VagrantStatus
case object Saved extends VagrantStatus
case object NotCreated extends VagrantStatus
case object Unknown extends VagrantStatus

object Vagrant {

  lazy val vagrantFile    = settingKey[File]("vagrant-file")
  val vagrant             = settingKey[Vagrant]("vagrant")
  lazy val vagrantUp      = taskKey[Unit]("vagrantUp")
  lazy val vagrantSuspend = taskKey[Unit]("vagrantSuspend")

  lazy val settings = Seq(
    vagrantFile     := (baseDirectory in ThisBuild).value / "Vagrantfile",
    vagrant         := new Vagrant(vagrantFile.value),
    vagrantUp      <<= (vagrant) map { (vagrant: Vagrant) => vagrant.setup() },
    vagrantSuspend <<= (vagrant) map { (vagrant: Vagrant) => vagrant.cleanup() }
  )
}

class Vagrant(vagrantFile: File) extends VagrantStatus {

  private val vagrantDir                = vagrantFile.getParentFile
  private var prevStatus: VagrantStatus = Unknown

  private def up(): Unit        = Process("vagrant" :: "up" :: Nil, vagrantDir) !
  private def suspend(): Unit   = Process("vagrant" :: "suspend" :: Nil, vagrantDir) !
  private def provision(): Unit = Process("vagrant" :: "provision" :: Nil, vagrantDir) !
  private def destroy(): Unit   = Process("vagrant" :: "destroy" :: "-f" :: Nil, vagrantDir) !

  def setup(): Unit = {
    prevStatus = status()
    prevStatus match {
      case Running    => provision() // to be changed later
      case NotCreated => up()
      case Unknown    => up()
    }
  }

  def cleanup(): Unit = if (prevStatus != Running) suspend()

  private def status(): VagrantStatus = {
    val res = Process("vagrant" :: "status" :: Nil, vagrantDir) !!

    if (res.contains("running")) Running
    else if (res.contains("not created")) NotCreated
    else Unknown
  }

}
