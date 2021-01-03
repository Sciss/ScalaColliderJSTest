import de.sciss.osc
import de.sciss.synth.{BuildInfo, Client, Server, ServerConnection, SynthDef, ugen}

import scala.scalajs.js.annotation.JSExportTopLevel

object ScalaColliderTest {
  @JSExportTopLevel("runScalaCollider")
  def main(): Unit = {
    println(s"Hello from ${BuildInfo.name} ${BuildInfo.version} on Scala ${BuildInfo.scalaVersion}.")
    val config = Server.Config()
    config.transport = osc.Browser
    val client = Client.Config()

    Server.connect("browser", config, client) {
      case ServerConnection.Preparing(_) =>
        println("<server preparing>")
      case ServerConnection.Running(s) =>
        println("<server running>")
        println(s.counts)
      case ServerConnection.Aborted =>
        println("<server abort>")
    }
  }

  @JSExportTopLevel("bubbles")
  def bubbles(): Unit = {
    import de.sciss.synth.Ops._
    val df1 = SynthDef("AnalogBubbles") {
      import ugen._
      val f1 = "freq1".kr(0.4)
      val f2 = "freq2".kr(8.0)
      val d  = "detune".kr(0.90375)
      val f  = LFSaw.ar(f1).mulAdd(24, LFSaw.ar(Seq(f2, f2 * d)).mulAdd(3, 80)).midiCps // glissando function
      val x  = CombN.ar(SinOsc.ar(f) * 0.04, 0.2, 0.2, 4) // echoing sine wave
      Out.ar(0, x)
    }
    /*val x1 =*/ df1.play()
  }

  @JSExportTopLevel("setControl")
  def setControl(name: String, value: Double): Unit = {
    import de.sciss.synth.Ops._
    Server.default.defaultGroup.set(name -> value)
  }

  @JSExportTopLevel("dumpOSC")
  def dumpOSC(code: Int = 1): Unit =
    Server.default.dumpOSC(osc.Dump(code))

  @JSExportTopLevel("dumpTree")
  def dumpTree(): Unit = {
    Server.default.dumpTree(controls = true)
  }

  @JSExportTopLevel("cmdPeriod")
  def cmdPeriod(): Unit = {
    import de.sciss.synth.Ops._
    Server.default.freeAll()
  }

  @JSExportTopLevel("serverCounts")
  def serverCounts(): Unit =
    println(Server.default.counts)
}