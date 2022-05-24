package de.unruh.stuff

import java.net.URI
import scala.util.matching.Regex

object ExtendedURL {
  val idRegex: Regex = "[1-9][0-9]*".r
  val fileRegex: Regex = "[a-z0-9_@,][a-z0-9_@,.-]*".r
  val schemaSpecificRegex: Regex = "([1-9][0-9]*)/([a-z0-9_@,][a-z0-9_@,.-]*)".r

  def resolve(username: String, url: URI): String = {
    if (url.getScheme == "localstuff") {
      url.getRawSchemeSpecificPart match {
        // TODO Url-encode username (including /'s)
        case schemaSpecificRegex(id, file) => s"/files/$username/${id}/${file}"
        case _ => throw new AssertionError(url.toString)
      }
    } else
      url.toString
  }
}
