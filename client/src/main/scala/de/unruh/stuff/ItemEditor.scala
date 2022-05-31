package de.unruh.stuff

import de.unruh.stuff.shared.{Item, RichText}
import io.kinoplan.scalajs.react.material.ui.core.MuiInput
import japgolly.scalajs.react.component.Scala.{Component, Unmounted}
import japgolly.scalajs.react.vdom.all.{className, div, href, img, li, onChange, onClick, src, textarea, value}
import japgolly.scalajs.react.vdom.{TagMod, VdomElement, all}
import japgolly.scalajs.react.{BackendScope, Callback, CtorType, ReactFormEventFromInput, ScalaComponent}
import monocle.macros.Lenses

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import japgolly.scalajs.react.vdom.Implicits._

import java.net.URI

object ItemEditor {
  case class Props(initialItem: Item, onSave: Callback)

  @Lenses case class State(editedItem: Item)

  def apply(props: Props): Unmounted[Props, State, Backend] = Component(props)

  def apply(initialItem: Item, onSave: Callback): Unmounted[Props, State, Backend] =
    apply(Props(initialItem = initialItem, onSave = onSave))

  private val itemName = State.editedItem.andThen(Item.name)
  private val itemDescription = State.editedItem.andThen(Item.description)

  private def url(url: URI) = ExtendedURL.resolve(JSVariables.username, url)

  class Backend(bs: BackendScope[Props, State]) {
    private val save = Callback {
      val state = bs.state.runNow()
      val props = bs.props.runNow()
      for (_ <- DbCache.updateItem(state.editedItem))
        yield props.onSave.runNow()
    }

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
          val images = for ((photo, i) <- item.photos.zipWithIndex)
            yield (img(src := url(photo)): VdomElement)
          div(className := "item-photos")(images: _*)
        } else
          TagMod.empty,

        // TODO rich text, e.g., https://lexical.dev/ (see also https://lexical.dev/docs/concepts/serialization#html)
        div(textarea(value := item.description.asHtml, className := "item-description",
          onChange ==> { event: ReactFormEventFromInput =>
            bs.modState(itemDescription.replace(RichText.html(event.target.value)))
          })),

        // TODO link editor
        // TODO Do we really need links if we have rich text descriptions?
        if (item.links.nonEmpty) {
          val links = for (link <- item.links)
            yield li(all.a(href := link.toString)(link.toString))
          div(className := "item-links")(links: _*)
        } else
          TagMod.empty,

        // TODO code editor
        if (item.codes.nonEmpty) {
          val codes = for ((link, i) <- item.codes.zipWithIndex)
            yield li(link.toString)
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
