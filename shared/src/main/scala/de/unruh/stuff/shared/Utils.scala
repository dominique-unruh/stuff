package de.unruh.stuff.shared

import java.nio.charset.Charset
import java.util.concurrent.atomic.AtomicInteger

object Utils {
  val utf8: Charset = Charset.forName("utf-8")
  def escapeFilename(filename: String): String = {
    val sanitized1 = filename.getBytes(utf8).map { b =>
      val c = b.toChar
      if (c <= 127 && (c.isLetterOrDigit || c==' ' || c=='.' || c=='_' || c=='@' || c=='-'))
        c.toString
      else
        f"%%$b%02x"
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
}
