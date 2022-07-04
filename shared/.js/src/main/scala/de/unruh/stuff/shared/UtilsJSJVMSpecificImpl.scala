package de.unruh.stuff.shared

import java.net.URI
import scala.scalajs.js

object UtilsJSJVMSpecificImpl extends UtilsJSJVMSpecific {
  override def encodeURIComponent(str: String): String = {
    js.URIUtils.encodeURIComponent(str)
  }
}
