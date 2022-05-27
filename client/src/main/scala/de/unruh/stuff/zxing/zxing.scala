package de.unruh.stuff

import scala.scalajs.js
import scala.scalajs.js.UndefOr

/** Facade for ZXing Javascript library.
 * See https://github.com/zxing-js/library/tree/master/src/ */
package object zxing {
  type DecodeContinuouslyCallback = js.Function2[Result, UndefOr[ZException], js.Any]
}
