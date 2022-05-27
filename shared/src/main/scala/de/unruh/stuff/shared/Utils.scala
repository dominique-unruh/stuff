package de.unruh.stuff.shared

object Utils {
  def joinClasses(classes: String*): String =
    classes.filter(cls => cls!=null && cls.nonEmpty).mkString(" ")

  def addSpaceIfNeeded(str: String) : String =
    if (str.isEmpty || str.last.isWhitespace)
      str
    else
      str + " "
}
