package de.unruh.stuff.materialui

import slinky.core.ExternalComponent

import scala.scalajs.js
import scala.scalajs.js.{UndefOr, |}

object Avatar extends ExternalComponent {
  case class Props(src: UndefOr[String] = js.undefined, variant: UndefOr[String] = js.undefined)

  override val component: String | js.Object = MaterialUi.Avatar

  val SQUARE = "square"
}
