package de.unruh.stuff.editor

import de.unruh.stuff.reactsimplewysiwyg.DefaultEditor
import de.unruh.stuff.shared.{Code, Item, RichText}
import de.unruh.stuff._
import de.unruh.stuff.editor.ItemEditor.itemCodes
import io.kinoplan.scalajs.react.material.ui.core.MuiInput
import japgolly.scalajs.react.callback.AsyncCallback
import japgolly.scalajs.react.component.Scala.{Component, Unmounted}
import japgolly.scalajs.react.component.builder.Lifecycle
import japgolly.scalajs.react.component.builder.Lifecycle.Lifecycle
import japgolly.scalajs.react.component.builder.LifecycleF.RenderScope
import japgolly.scalajs.react.extra.internal.StateSnapshot
import japgolly.scalajs.react.vdom.all.{button, className, div, img, onChange, onClick, src, value}
import japgolly.scalajs.react.vdom.{TagMod, VdomElement, all}
import japgolly.scalajs.react.{BackendScope, Callback, CtorType, ReactFormEventFromInput, ScalaComponent}
import monocle.macros.Lenses

import java.net.URI
import scala.util.{Failure, Success}
import japgolly.scalajs.react.vdom.Implicits._
import monocle.Lens

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue



object ItemEditor {
  private type RS = Lifecycle.RenderScope[Props, State, Unit]
  case class Props(initialItem: Item, onSave: Item => Callback, onCancel: Callback)

  @Lenses case class State(editedItem: Item)

  def apply(props: Props): Unmounted[Props, State, Unit] = Component(props)

  def apply(initialItem: Item, onSave: Item => Callback, onCancel: Callback): Unmounted[Props, State, Unit] =
    apply(Props(initialItem = initialItem, onSave = onSave, onCancel = onCancel))

  private val itemName = State.editedItem.andThen(Item.name)
  private val itemCodes = State.editedItem.andThen(Item.codes)
  private val itemDescription = State.editedItem.andThen(Item.description)
  private val itemPhotos = State.editedItem.andThen(Item.photos)

  private def zoomL[A](lens: Lens[State, A]) = StateSnapshot.zoom(lens.get)(lens.replace)

  /** Saves the edited item, notifies the user, and calls `props.onSave`. */
  private def save(implicit $: RS): AsyncCallback[Unit] = {
    val state = $.state
    val props = $.props
    for (result <- DbCache.updateOrCreateItemReact(state.editedItem).attemptTry;
         _ <- result match {
           case Success(id) =>
             for (item <- AsyncCallback.fromFuture(DbCache.getItem(id)); // Important to use the id returned from server. It might be fresh if we edited a new item
                  // This reloads the item. Important because the server processes the item in .updateItems
                  _ <- $.modState(state => state.copy(editedItem = item)).asAsyncCallback;
                  _ <- AppMain.successMessage("Saved").asAsyncCallback;
                  _ <- props.onSave(item).asAsyncCallback)
             yield {}
           case Failure(exception) =>
             AppMain.errorMessage("Failed to save item", exception).asAsyncCallback
         }
         )
    yield {}
  }

  /** Sets a new location and hides the search dialog */
  private def setLocation(id: Item.Id)(implicit $: RS): Callback =
    $.modState(state => state.copy(editedItem = state.editedItem.setLocation(id)))

  private def remove(implicit $: RS): Callback =
    $.modState(state => state.copy(state.editedItem.clearLocation()))

  private def putLocationElement(implicit $: RS): VdomElement = ModalAction[Item.Id](
    onAction = setLocation _,
    button = { (put: Callback) => button("Put", onClick --> put): VdomElement },
    modal = { (action: Item.Id => Callback) =>
      // TODO: in search results, show prevLocation first
      ItemSearch(visible = true,
        onCreate = None,
        onSelectItem = action,
        showCreate = false,
      ): VdomElement
    })

  def render(implicit $: RS): VdomElement = {
    val item = $.state.editedItem
    div(className := "item-editor state-editing",
      div(all.button("Save", onClick --> save), all.button("Cancel", onClick --> $.props.onCancel)),
      MuiInput(
        //          inputProps = js.Dynamic.literal(`type`="search"),
        /*inputProps = MuiInputProps(ariaLabel = "Description")*/
      )(
        className := "item-name",
        onChange ==> { event: ReactFormEventFromInput => $.modState(itemName.replace(event.target.value)) },
        value := item.name
      ),

      PhotosEditor(zoomL(itemPhotos) of $),

      item.location match {
        case Some(location) =>
          div("Location:", putLocationElement, button("Remove", onClick --> remove),
            ItemListItem(location, onClick = { _ => Callback.empty }))
        case None =>
          div("Location:", putLocationElement)
      },

      div(DefaultEditor(value = item.description.asHtml, onChange = { event =>
        $.modState(itemDescription.replace(RichText.html(event.target.value))).runNow()
      })
      (className := "item-description")),

      CodesEditor(zoomL(itemCodes) of $),

      div(className := "item-id", item.id.toString),
    )
  }

  val Component: Component[Props, State, Unit, CtorType.Props] =
    ScalaComponent.builder[Props]
      .initialStateFromProps(props => State(editedItem = props.initialItem))
      .render(render(_))
      .build
}
