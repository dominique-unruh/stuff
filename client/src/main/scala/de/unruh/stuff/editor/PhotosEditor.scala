package de.unruh.stuff.editor

import de.unruh.stuff.editor.CodesEditor.Props
import de.unruh.stuff.editor.ItemEditor.itemPhotos
import de.unruh.stuff.{Camera, ExtendedURL, ItemSearch, JSVariables, ModalAction}
import de.unruh.stuff.shared.Code
import japgolly.scalajs.react.component.Scala.{Component, Unmounted}
import japgolly.scalajs.react.vdom.{TagMod, VdomElement}
import japgolly.scalajs.react.vdom.all.{button, className, div, img, onClick, src}
import japgolly.scalajs.react.{Callback, CtorType, ScalaComponent}

import java.net.URI
import japgolly.scalajs.react.vdom.Implicits._

object PhotosEditor {
  case class Props(photos: Seq[URI], change: (Seq[URI] => Seq[URI]) => Callback)

  def apply(photos: Seq[URI], change: (Seq[URI] => Seq[URI]) => Callback): Unmounted[Props, Unit, Unit] =
    Component(Props(photos = photos, change = change))

  private def url(url: URI) = ExtendedURL.resolve(JSVariables.username, url)

  private def addPhoto(photo: String)(implicit props: Props): Callback = {
    val url = URI.create(photo)
    assert(url.getScheme == "data")
    props.change(_.appended(url))
  }

  val Component: Component[Props, Unit, Unit, CtorType.Props] = ScalaComponent.builder[Props]
    .stateless
    .render_P { implicit props =>
      div(
        // TODO allow removing pictures
        if (props.photos.nonEmpty) {
          val images = for (photo <- props.photos)
            yield (img(src := url(photo)): VdomElement)
          div(className := "item-photos")(images: _*)
        } else
          TagMod.empty,

        ModalAction[String](
          button = { (open: Callback) => div(button(onClick --> open)("Add photo")): VdomElement },
          modal = { (onPhoto: String => Callback) => Camera(onPhoto = onPhoto): VdomElement },
          onAction = addPhoto _
        ),
      )
    }
    .build
}
