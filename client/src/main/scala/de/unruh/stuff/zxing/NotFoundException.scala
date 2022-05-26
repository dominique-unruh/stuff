package de.unruh.stuff.zxing

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

@js.native
@JSImport("@zxing/library")
class NotFoundException protected () extends ZException
