package de.unruh.stuff.shared

import java.net.URI
import java.nio.charset.Charset
import java.util.concurrent.atomic.AtomicInteger

object Utils extends UtilsJSJVMSpecific {
  val utf8: Charset = Charset.forName("utf-8")
  def escapeFilename(filename: String): String = {
    val sanitized1 = filename.getBytes(utf8).map { b =>
      val c = b.toChar
      if (c <= 127 && (c.isLetterOrDigit || c==' ' || c=='.' || c=='_' || c=='@' || c=='-'))
        c.toString
      else
        f"%%${b & 0xFF}%02x" // "& 0xFF" is needed on ScalaJS, otherwise it renders a negative int here
    }.mkString
    if (sanitized1.startsWith("."))
      "%2e" + sanitized1.stripPrefix(".")
    else if (sanitized1 == "")
      "%__"
    else
      sanitized1
  }

  def addSpaceIfNeeded(str: String) : String =
    if (str.isEmpty || str.last.isWhitespace)
      str
    else
      str + " "

  private val atomicInteger = new AtomicInteger(0)
  def uniqueId() : Int = atomicInteger.incrementAndGet()

//  private val jsjvmSpecific : UtilsJSJVMSpecific =
//    getClass.getClassLoader.loadClass("de.unruh.stuff.shared.UtilsImpl").asInstanceOf[UtilsJSJVMSpecific]

  override def encodeURIComponent(str: String): String =
    UtilsJSJVMSpecificImpl.encodeURIComponent(str)
}

trait UtilsJSJVMSpecific {
  def encodeURIComponent(name: String) : String
}
