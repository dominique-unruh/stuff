package de.unruh.stuff.zxing

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

@js.native
@JSImport("@zxing/library", "Exception")
class ZException protected () extends CustomError {
  def getKind(): String = js.native
}
