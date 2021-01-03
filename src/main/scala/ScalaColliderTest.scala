/*
 *  ScalaColliderTest.scala
 *  (ScalaCollider JS Test)
 *
 *  Copyright (c) 2021 Hanns Holger Rutz. All rights reserved.
 *
 *  This software is published under the GNU Affero General Public License v3+
 *
 *
 *  For further information, please contact Hanns Holger Rutz at
 *  contact@sciss.de
 */

import de.sciss.{numbers, osc}
import de.sciss.synth.{Buffer, BuildInfo, Client, GE, Server, ServerConnection, SynthDef, freeSelf, ugen}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js
import scala.scalajs.js.annotation.{JSExportTopLevel, JSGlobal}

object ScalaColliderTest {
  val BUILD_NUMBER  = 3
  val PROJECT_URL   = "https://github.com/Sciss/ScalaColliderJSTest"

  def any2stringadd: Any = ()

  @js.native
  @JSGlobal("Module")
  object Module extends js.Object {
    def print: js.Function1[String, Unit] = js.native
  }

  def print_ui(s: String): Unit =
    Module.print(s)

  @JSExportTopLevel("runScalaCollider")
  def main(): Unit = {
    print_ui(
      s"""Hello from ${BuildInfo.name} ${BuildInfo.version} on Scala ${BuildInfo.scalaVersion}.
         |Test build: $BUILD_NUMBER
         |For source code, visit $PROJECT_URL
         |""".stripMargin
    )
    val config = Server.Config()
    config.transport = osc.Browser
    val client = Client.Config()

    Server.connect("browser", config, client) {
      case ServerConnection.Preparing(_) =>
        println("<server preparing>")
      case ServerConnection.Running(s) =>
        print_ui("Server connected:")
        print_ui(s.counts.toString)
        print_ui(
          """
            |From the browser (ctrl-shift-J in Chrome) console, try executing `example('bubbles')`.
            |Execute `cmdPeriod()` to stop all sounds.
            |Execute `sendOSC(name, args...)` to send arbitrary OSC messages to the server.
            |""".stripMargin
        )
      case ServerConnection.Aborted =>
        println("<server abort>")
    }
  }

  trait Example { def run(): Unit }

  case class ExSynthDef(name: String)(body: => Unit) extends Example {
    def run(): Unit = {
      import de.sciss.synth.Ops._
      val df = SynthDef(name)(body)
      df.play()
    }
  }

  val examples: Map[String, Example] = {
    import de.sciss.synth.Ops._
    import numbers.Implicits._
    import ugen._
    Map(
      // most of these examples come originally from James McCartney
      "bubbles" -> ExSynthDef("bubbles") {
        val f1 = "freq1".kr(0.4)
        val f2 = "freq2".kr(8.0)
        val d  = "detune".kr(0.90375)
        val f  = LFSaw.ar(f1).mulAdd(24, LFSaw.ar(Seq(f2, f2 * d)).mulAdd(3, 80)).midiCps // glissando function
        val x  = CombN.ar(SinOsc.ar(f) * 0.04, 0.2, 0.2, 4) // echoing sine wave
        Out.ar(0, x)
      },
      "harm-swim" -> ExSynthDef("harm-swim") {
        val f = "freq".kr(50) // fundamental frequency
        val p = 20            // number of partials per channel
        val offset = Line.kr(0, -0.02, 60, doneAction = freeSelf) // causes sound to separate and fade
        val sig = Mix.tabulate(p) { i =>
          FSinOsc.ar(f * (i+1)) * // freq of partial
            LFNoise1.kr(Seq(Rand(2, 10), Rand(2, 10)))  // amplitude rate
              .mulAdd(
                0.02,     // amplitude scale
                offset    // amplitude offset
              ).max(0)    // clip negative amplitudes to zero
        }
        Out.ar(0, sig)
      },
      "harm-tumb" -> ExSynthDef("harm-tumb") {
        val f = "freq".kr(80) // fundamental frequency
        val p = 10            // number of partials per channel
        val trig = XLine.kr(Seq(10, 10), 0.1, 60, doneAction = freeSelf) // trigger probability decreases over time
        val sig = Mix.tabulate(p){ i =>
          FSinOsc.ar(f * (i+1)) *    // freq of partial
            Decay2.kr(
              Dust.kr(trig) // trigger rate
                * 0.02,       // trigger amplitude
              0.005,        // grain attack time
              Rand(0,0.5)   // grain decay time
            )
        }
        Out.ar(0, sig)
      },
      "what-think" -> ExSynthDef("what-think") {
        val f = "freq".kr(500)
        val z = RLPF.ar(
          Pulse.ar(
            SinOsc.kr(4).mulAdd(1, 80).max(
              Decay.ar(LFPulse.ar(0.1, 0, 0.05) * Impulse.ar(8) * f, 2)
            ),
            LFNoise1.kr(0.157).mulAdd(0.4, 0.5)
          ) * 0.04,
          LFNoise1.kr(0.2).mulAdd(2000, 2400),
          0.2
        )
        val y = z * 0.6
        val sig = z + Seq(
          CombL.ar(y, 0.06, LFNoise1.kr(Rand(0, 0.3)).mulAdd(0.025, 0.035), 1)
            + CombL.ar(y, 0.06, LFNoise1.kr(Rand(0, 0.3)).mulAdd(0.025, 0.035), 1),
          CombL.ar(y, 0.06, LFNoise1.kr(Rand(0, 0.3)).mulAdd(0.025, 0.035), 1)
            + CombL.ar(y, 0.06, LFNoise1.kr(Rand(0, 0.3)).mulAdd(0.025, 0.035), 1)
        )
        Out.ar(0, sig)
      },
      "police" -> ExSynthDef("police") {
        val n = 4   // number of sirens
        val sig = CombL.ar(
          Mix.fill(n) {
            Pan2.ar(
              SinOsc.ar(
                SinOsc.kr(Rand(0.02, 0.12), Rand(0, 2*math.Pi)).mulAdd(IRand(0, 599), IRand(700, 1299))
              ) * LFNoise2.ar(Rand(80, 120)) * 0.1,
              Rand(-1, 1)
            )
          }
            + LFNoise2.ar(
            LFNoise2.kr(Seq(0.4, 0.4)).mulAdd(90, 620)) *
            LFNoise2.kr(Seq(0.3, 0.3)).mulAdd(0.15, 0.18),
          0.3, 0.3, 3
        )
        Out.ar(0, sig)
      },
      "cymbal" -> ExSynthDef("cymbal") {
        val p = 15   // number of partials per channel per 'cymbal'.
        val f1 = Rand(500, 2500)
        val f2 = Rand(0, 8000)
        val sig = for (_ <- 1 to 2) yield {
          val z = KlangSpec.fill(p) {
            // sine oscillator bank specification :
            (f1 + Rand(0, f2),  // frequencies
              1,                 // amplitudes
              Rand(1, 5))        // ring times
          }
          Klank.ar(z, Decay.ar(Impulse.ar(Rand(0.5, 3.5)), 0.004) * WhiteNoise.ar(0.03))
        }
        Out.ar(0, sig)
      },
      "walters" -> ExSynthDef("walters") {
        // original example by Tim Walters -- a bit heavy on the CPU
        val sig = GVerb.ar(LeakDC.ar(
          Mix.tabulate(6 /*16*/) { k =>
            Mix.tabulate(6) { i =>
              val x = Impulse.kr(0.5.pow(i) / k)
              SinOsc.ar(i, SinOsc.ar((i + k).pow(i)) / (Decay.kr(x, Seq(i, i+1)) * k))
            }
          }
        ), roomSize = 1) / 200 /*384*/
        Out.ar(0, sig)
      },
      "pitch" -> ExPitch,
      "part-conv" -> ExPartConv,
    )
  }

  @JSExportTopLevel("example")
  def example(name: String): Unit =
    examples.get(name) match {
      case Some(ex) => ex.run()
      case None =>
        println(s"Unknown example '$name'")
        println(examples.keys.toSeq.sorted.mkString("Try one of: ", ", ", ""))
    }

  object ExPartConv extends Example {
    def run(): Unit = {
      import de.sciss.numbers.Implicits._
      import de.sciss.synth.Ops._
      // Dan Stowell's reverb
      // synthesize impulse response
      val ir = (1f +: Vector.fill(100)(0f)) ++ Vector.tabulate(50000) { i =>
        if (math.random() < 0.5) 0f else {
          val f = 1f - i / 50000f
          f.pow(8) * (if (math.random() < 0.5) -0.1f else +0.1f)
        }
      }

      // calculate the partitioning parameters
      val fftSize  = 2048
      val numPart  = (ir.size * 2.0 / fftSize).ceil.toInt  // 49
      val partSize = fftSize * numPart  // 100352

      // send the IR to a regular buffer
      val s     = Server.default
      val irBuf = Buffer(s)

      // create the specially formatted partitioned buffer
      val partBuf  = Buffer(s)

      for {
        _ <- {
          irBuf.alloc(ir.size)
          irBuf.setData(ir)
        }
        _ <- partBuf.alloc(partSize)
        _ <- {
          s ! osc.Message("/b_gen", partBuf.id, "PreparePartConv", irBuf.id, fftSize)
          s.sync()
        }
      } yield {
        // now we can forget about the input buffer
        irBuf.free()

        val df = SynthDef("part-conv") {
          import ugen._
          // trigger IR every 4 seconds
          val in = Impulse.ar(0.25) * 0.5
          val sig = PartConv.ar(in, fftSize, partBuf.id)
          Out.ar(0, Pan2.ar(sig))
        }
        val x = df.play(s)

        // do not forget to free the buffer eventually
        x.onEnd {
          partBuf .free()
        }
      }
    }
  }

  object ExPitch extends Example {
    def run(): Unit = {
      print_ui("Note: this synth needs microphone input. Server must be booted with number of inputs > 0!")
      ExSynthDef("pitch") {
        import ugen._
        val in    = Mix(PhysicalIn.ar(0, 2))
        val amp   = Amplitude.kr(in, 0.05, 0.05)
        val pitch = Pitch.kr(in, ampThresh = 0.02, median = 7)
        var sound: GE = Mix(VarSaw.ar(pitch.freq * Seq(0.5, 1, 2), width =
          LFNoise1.kr(0.3).mulAdd(0.1, 0.1)) * amp)
        for (_ <- 0 until 6) {
          sound = AllpassN.ar(sound, 0.040, Seq.fill(2)(Rand(0.0, 0.040)), 2)
        }
        Out.ar(0, sound)
      } .run()
    }
  }

  @JSExportTopLevel("dumpOSC")
  def dumpOSC(code: Int = 1): Unit =
    Server.default.dumpOSC(osc.Dump(code))

  @JSExportTopLevel("dumpTree")
  def dumpTree(): Unit = {
    import de.sciss.synth.Ops._
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

  @JSExportTopLevel("sendOSC")
  def sendOSC(cmd: String, args: js.Any*): Unit =
    Server.default.!(osc.Message(cmd, args: _*))
}