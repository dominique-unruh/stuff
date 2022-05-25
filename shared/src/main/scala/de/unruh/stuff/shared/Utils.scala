package de.unruh.stuff.shared

object Utils {
  def addSpaceIfNeeded(str: String) : String =
    if (str.isEmpty || str.last.isWhitespace)
      str
    else
      str + " "
}
