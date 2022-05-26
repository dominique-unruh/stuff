package de.unruh.stuff.zxing

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

@js.native
@JSImport("@zxing/library")
class Result protected () extends js.Object {
  /**
   * @return raw text encoded by the barcode
   */
  def getText(): String = js.native

  /**
   * @return {@link BarcodeFormat} representing the format of the barcode that was decoded
   */
  def getBarcodeFormat(): BarcodeFormat.T = js.native
}
