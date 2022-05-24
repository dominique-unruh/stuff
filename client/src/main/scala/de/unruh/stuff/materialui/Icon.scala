package de.unruh.stuff.materialui

import slinky.core.ExternalComponentNoProps

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport
import scala.scalajs.js.|

@JSImport("@mui/icons-material", JSImport.Default)
@js.native
object IconsMaterial extends js.Object {
  val IceSkating: js.Object = js.native
}

class Icon(icon: js.Object) extends ExternalComponentNoProps {
  override val component: String | js.Object = icon
}
object Icon {
  def apply(icon: js.Object): Icon = new Icon(icon)
}
