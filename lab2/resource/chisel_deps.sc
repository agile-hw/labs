interp.repositories() ::: List(
  coursierapi.MavenRepository.of("https://oss.sonatype.org/content/repositories/snapshots")
)

// We must compile to scala 2.11.12 due to a change in type inference between Scala 2.11 and 2.12. To remain compatible with Chisel3, just stick to 2.11
interp.configureCompiler(x => x.settings.source.value = scala.tools.nsc.settings.ScalaVersion("2.11.12"))


// Getting the newer chisel import versions are from this repo below
//interp.repositories() ++= Seq(
//  coursierapi.MavenRepository
//    .of("https://maven.imagej.net/content/repositories/public/")
//)

import $ivy.`edu.berkeley.cs::chisel3:3.4.+`
import $ivy.`edu.berkeley.cs::chisel-iotesters:1.5.+`
import $ivy.`edu.berkeley.cs::chiseltest:0.3.+`
import $ivy.`edu.berkeley.cs::dsptools:1.4.+`
import $ivy.`org.scalanlp::breeze:0.13.2`
import $ivy.`edu.berkeley.cs::rocket-dsptools:1.2.0`
import $ivy.`edu.berkeley.cs::firrtl-diagrammer:1.3.+`

import $ivy.`org.scalatest::scalatest:3.2.2`


// These are the imports for digrammer (requires the sonatype repo above)
import $ivy.`edu.berkeley.cs::dsptools:1.2.0`
import $ivy.`org.scalanlp::breeze:0.13.2`
import $ivy.`edu.berkeley.cs::rocket-dsptools:1.2.0`
import $ivy.`edu.berkeley.cs::firrtl-diagrammer:1.1.0`

// Convenience function to invoke Chisel and grab emitted Verilog.
def getVerilog(dut: => chisel3.core.UserModule): String = {
  import firrtl._
  return chisel3.Driver.execute(Array[String](), {() => dut}) match {
    case s:chisel3.ChiselExecutionSuccess => s.firrtlResultOption match {
      case Some(f:FirrtlExecutionSuccess) => f.emitted
    }
  }
}

// Convenience function to invoke Chisel and grab emitted FIRRTL.
def getFirrtl(dut: => chisel3.core.UserModule): String = {
  return chisel3.Driver.emit({() => dut})
}

def compileFIRRTL(
    inputFirrtl: String,
    compiler: firrtl.Compiler,
    customTransforms: Seq[firrtl.Transform] = Seq.empty,
    infoMode: firrtl.Parser.InfoMode = firrtl.Parser.IgnoreInfo,
    annotations: firrtl.AnnotationSeq = firrtl.AnnotationSeq(Seq.empty)
): String = {
  import firrtl.{Compiler, AnnotationSeq, CircuitState, ChirrtlForm, FIRRTLException}
  import firrtl.Parser._
  import scala.io.Source
  import scala.util.control.ControlThrowable
  import firrtl.passes._
  val outputBuffer = new java.io.CharArrayWriter
  try {
      //val parsedInput = firrtl.Parser.parse(Source.fromFile(input).getLines(), infoMode)
      val parsedInput = firrtl.Parser.parse(inputFirrtl.split("\n").toIterator, infoMode)
      compiler.compile(
         CircuitState(parsedInput, ChirrtlForm, annotations),
         outputBuffer,
         customTransforms)
  }

  catch {
    // Rethrow the exceptions which are expected or due to the runtime environment (out of memory, stack overflow)
    case p: ControlThrowable => throw p
    case p: PassException  => throw p
    case p: FIRRTLException => throw p
     // Treat remaining exceptions as internal errors.
       case e: Exception => firrtl.Utils.throwInternalError(exception = Some(e))
  }

  val outputString = outputBuffer.toString
  outputString
}

def stringifyAST(firrtlAST: firrtl.ir.Circuit): String = {
  var ntabs = 0
  val buf = new StringBuilder
  val string = firrtlAST.toString
  string.zipWithIndex.foreach { case (c, idx) =>
    c match {
      case ' ' =>
      case '(' =>
        ntabs += 1
        buf ++= "(\n" + "| " * ntabs
      case ')' =>
        ntabs -= 1
        buf ++= "\n" + "| " * ntabs + ")"
      case ','=> buf ++= ",\n" + "| " * ntabs
      case  c if idx > 0 && string(idx-1)==')' =>
        buf ++= "\n" + "| " * ntabs + c
      case c => buf += c
    }
  }
  buf.toString
}


