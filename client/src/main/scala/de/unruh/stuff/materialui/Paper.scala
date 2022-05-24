package de.unruh.stuff.materialui

import slinky.core.ExternalComponent

import scala.scalajs.js
import scala.scalajs.js.{UndefOr, |}

object Paper extends ExternalComponent {
  case class Props(elevation: UndefOr[Int] = js.undefined,
                   square: UndefOr[Boolean] = js.undefined,
                   variant: UndefOr[String] = js.undefined,
                  )
  override val component: String | js.Object = MaterialUi.Paper

  val ELEVATION = "elevation"
  val OUTLINED = "outlined"
}
