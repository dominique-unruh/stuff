package de.unruh.stuff.materialui

import slinky.core.{ExternalComponent, ExternalComponentNoProps}

import scala.scalajs.js
import scala.scalajs.js.{UndefOr, |}

object ImageList extends ExternalComponent {
  case class Props(cols: UndefOr[Int] = js.undefined,
                   gap: UndefOr[Int | Double] = js.undefined,
                   rowHeight: UndefOr[Double] = js.undefined,
                   variant: UndefOr[String] = js.undefined
                  )
  override val component: String | js.Object = MaterialUi.ImageList

  val MASONRY = "masonry"
  val QUILTED = "quilted"
  val STANDARD = "standard"
  val WOVEN = "woven"
}

object ImageListItem extends ExternalComponentNoProps {
  override val component: String | js.Object = MaterialUi.ImageListItem
}