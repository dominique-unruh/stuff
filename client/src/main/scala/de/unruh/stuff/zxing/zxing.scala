package de.unruh.stuff

import scala.scalajs.js
import scala.scalajs.js.UndefOr

/** Facade for ZXing Javascript library */
package object zxing {
  type DecodeContinuouslyCallback = js.Function2[Result, UndefOr[ZException], js.Any]
}
