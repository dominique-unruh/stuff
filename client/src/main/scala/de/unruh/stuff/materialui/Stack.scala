package de.unruh.stuff.materialui

import slinky.core.ExternalComponent
import slinky.core.facade.ReactElement

import scala.scalajs.js
import scala.scalajs.js.{UndefOr, |}

object Stack extends ExternalComponent {
  case class Props(direction: UndefOr[String] = js.undefined,
                   divider: UndefOr[ReactElement] = js.undefined,
                   spacing: UndefOr[String | Int | Double] = js.undefined,
                  )
  override val component: String | js.Object = MaterialUi.Stack

  val COLUMN = "column"
  val COLUMN_REVERSE = "column-reverse"
  val ROW = "row"
  val ROW_REVERSE = "row-reverse"
}
