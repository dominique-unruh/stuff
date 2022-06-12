package de.unruh.stuff.editor

import de.unruh.stuff.editor.CodesEditor.Props
import de.unruh.stuff.editor.ItemEditor.itemPhotos
import de.unruh.stuff.{Camera, ExtendedURL, ItemSearch, JSVariables, ModalAction}
import de.unruh.stuff.shared.Code
import japgolly.scalajs.react.component.Scala.{Component, Unmounted}
import japgolly.scalajs.react.extra.StateSnapshotF.StateSnapshot
import japgolly.scalajs.react.vdom.{TagMod, VdomElement}
import japgolly.scalajs.react.vdom.all.{button, className, div, img, onClick, span, src}
import japgolly.scalajs.react.{Callback, CtorType, ScalaComponent}

import java.net.URI
import japgolly.scalajs.react.vdom.Implicits._

object PhotosEditor {
  case class Props(photos: StateSnapshot[Seq[URI]])

  def apply(photos: StateSnapshot[Seq[URI]]): Unmounted[Props, Unit, Unit] =
    Component(Props(photos = photos))

  private def url(url: URI) = ExtendedURL.resolve(JSVariables.username, url)

  private def removePhoto(photo: URI)(implicit props: Props): Callback =
    props.photos.modState(_.filterNot(_ == photo))

  private def addPhoto(photo: String)(implicit props: Props): Callback = {
    val url = URI.create(photo)
    assert(url.getScheme == "data")
    props.photos.modState(_.appended(url))
  }

  val Component: Component[Props, Unit, Unit, CtorType.Props] = ScalaComponent.builder[Props]
    .stateless
    .render_P { implicit props =>
      div(
        if (props.photos.value.nonEmpty) {
          val images = for (photo <- props.photos.value)
            yield span(img(src := url(photo)),
              button("X", onClick --> removePhoto(photo)))
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
