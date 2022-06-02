package de.unruh.stuff

import de.unruh.stuff.reactsimplewysiwyg.DefaultEditor
import de.unruh.stuff.shared.{Item, RichText}
import io.kinoplan.scalajs.react.material.ui.core.MuiInput
import japgolly.scalajs.react.component.Scala.{Component, Unmounted}
import japgolly.scalajs.react.vdom.all.{button, className, div, href, img, li, onChange, onClick, src, textarea, value}
import japgolly.scalajs.react.vdom.{TagMod, VdomElement, all}
import japgolly.scalajs.react.{BackendScope, Callback, CtorType, ReactFormEventFromInput, ScalaComponent}
import monocle.macros.Lenses

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import japgolly.scalajs.react.vdom.Implicits._
import org.scalajs.dom.console

import java.net.URI

object ItemEditor {
  case class Props(initialItem: Item, onSave: Callback)

  @Lenses case class State(editedItem: Item, cameraOpen: Boolean = false)

  def apply(props: Props): Unmounted[Props, State, Backend] = Component(props)

  def apply(initialItem: Item, onSave: Callback): Unmounted[Props, State, Backend] =
    apply(Props(initialItem = initialItem, onSave = onSave))

  private val itemName = State.editedItem.andThen(Item.name)
  private val itemDescription = State.editedItem.andThen(Item.description)
  private val itemPhotos = State.editedItem.andThen(Item.photos)

  private def url(url: URI) = ExtendedURL.resolve(JSVariables.username, url)

  class Backend(bs: BackendScope[Props, State]) {
    private val save = Callback {
      val state = bs.state.runNow()
      val props = bs.props.runNow()
      for (_ <- DbCache.updateItem(state.editedItem))
        yield props.onSave.runNow()
    }

    private def cameraOnPhoto(photo: String) : Callback = {
      val url = URI.create(photo)
      assert(url.getScheme == "data")
      val modify = State.cameraOpen.replace(false)
        .andThen(itemPhotos.modify(_.appended(url)))
      for (_ <- bs.modState(modify))
        yield {}
    }

    private val cameraOnClose : Callback =
      bs.modState(_.copy(cameraOpen=false))

    private val openCamera : Callback =
      bs.modState(_.copy(cameraOpen=true))

    def render(props: Props, state: State): VdomElement = {
      val item = state.editedItem
      div(className := "item-editor state-editing",
        div(all.button("Save", onClick --> save)),
        MuiInput(
          //          inputProps = js.Dynamic.literal(`type`="search"),
          /*inputProps = MuiInputProps(ariaLabel = "Description")*/
        )(
          className := "item-name",
          onChange ==> { event: ReactFormEventFromInput => bs.modState(itemName.replace(event.target.value)) },
          value := item.name
        ),

        // TODO photo editor
        if (item.photos.nonEmpty) {
          val images = for (photo <- item.photos)
            yield (img(src := url(photo)): VdomElement)
          div(className := "item-photos")(images: _*)
        } else
          TagMod.empty,

        // This will show as a modal popup when "open = true"
        Camera(open = state.cameraOpen, onPhoto=cameraOnPhoto, onClose = cameraOnClose),

        div(button(onClick --> openCamera)("Add photo")),

        div(DefaultEditor(value = item.description.asHtml, onChange = { event =>
          bs.modState(itemDescription.replace(RichText.html(event.target.value))).runNow() })
          (className := "item-description")),

        // TODO code editor
        if (item.codes.nonEmpty) {
          val codes = for (code <- item.codes)
            yield li(code.toString)
          div(className := "item-codes")(codes: _*)
        } else
          TagMod.empty,

        div(className := "item-id", item.id.toString),
      )
    }
  }

  val Component: Component[Props, State, Backend, CtorType.Props] = ScalaComponent.builder[Props]
    .initialStateFromProps(props => State(editedItem = props.initialItem))
    .renderBackend[Backend]
    .build
}
