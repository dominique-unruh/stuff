package de.unruh.stuff.materialui

import slinky.core.ExternalComponent

import scala.scalajs.js
import scala.scalajs.js.{UndefOr, |}

object Typography extends ExternalComponent {
  case class Props(align: UndefOr[String] = js.undefined,
                   gutterBottom: UndefOr[Boolean] = js.undefined,
                   noWrap: UndefOr[Boolean] = js.undefined,
                   paragraph: UndefOr[Boolean] = js.undefined,
                   variant: UndefOr[String] = js.undefined,
                  )
  override val component: String | js.Object = MaterialUi.Typography

  val CAPTION = "caption"
  val H1 = "h1"
  val H2 = "h2"
}
