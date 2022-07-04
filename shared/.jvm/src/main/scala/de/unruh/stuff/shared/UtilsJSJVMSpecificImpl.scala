package de.unruh.stuff.shared

import java.net.{URI, URLEncoder}

object UtilsJSJVMSpecificImpl extends UtilsJSJVMSpecific {
  override def encodeURIComponent(str: String): String =
    URLEncoder.encode(str, Utils.utf8).replace("+", "%20")
}
