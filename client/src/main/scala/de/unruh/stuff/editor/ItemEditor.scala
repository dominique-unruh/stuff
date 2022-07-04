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
import org.log4s

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue



object ItemEditor {
  private type RS = Lifecycle.RenderScope[Props, State, Unit]
  case class Props(initialItem: Item, onSave: Item => Callback, onCancel: Callback)

  @Lenses case class State(/** Contains the current editing status.
                            The [[Item.description]] is processed to contain browser-understandable links
                            instead of extended item URLs. */
                            editedItem: Item)

  def apply(props: Props): Unmounted[Props, State, Unit] = Component(props)

  def apply(initialItem: Item, onSave: Item => Callback, onCancel: Callback): Unmounted[Props, State, Unit] =
    apply(Props(initialItem = initialItem, onSave = onSave, onCancel = onCancel))

  private val itemName = State.editedItem.andThen(Item.name)
  private val itemCodes = State.editedItem.andThen(Item.codes)
  private val itemDescription = State.editedItem.andThen(Item.description)
  private val itemPhotos = State.editedItem.andThen(Item.photos)
  private val itemDescriptionHtml = itemDescription.andThen(RichText.htmlLens)

  private def zoomL[A](lens: Lens[State, A]) = StateSnapshot.zoom(lens.get)(lens.replace)

  /** Converts an URL into an extended URL */
  private def extendedUrl(url: URI) : URI =
    ExtendedURLClient.externalize(JSVariables.username, url)
  private def browserUrl(url: URI) : URI =
    ExtendedURLClient.resolve(JSVariables.username, url)

  /** Saves the edited item, notifies the user, and calls `props.onSave`. */
  private def save(implicit $: RS): AsyncCallback[Unit] = {
    val state = $.state
    val props = $.props
    val processedItem =
      Item.description.andThen(RichText.htmlLens)
      .modify(html => ProcessHtml.mapUrls(html, extendedUrl))
      .apply(state.editedItem)
    for (result <- DbCache.updateOrCreateItemReact(processedItem).attemptTry;
         _ <- result match {
           case Success((id, time)) =>
             for (item <- AsyncCallback.fromFuture(DbCache.getItem(id, time)); // Important to use the id returned from server. It might be fresh if we edited a new item
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

  /** Sets a new location */
  private def setLocation(id: Item.Id)(implicit $: RS): Callback =
      for (_ <- $.modState(state => state.copy(editedItem = state.editedItem.setLocation(id)));
         _ = DbCache.touchLastModified(id))
      yield {}

  private def remove(implicit $: RS): Callback =
    $.modState(state => state.copy(state.editedItem.clearLocation))

  private def putLocationElement(implicit $: RS): VdomElement = ModalAction[Item.Id](
    key = "put-location",
    onAction = setLocation(_).asAsyncCallback,
    button = { (put: Callback) => button("Put", onClick --> put): VdomElement },
    modal = { (action: Item.Id => AsyncCallback[Unit]) =>
      ItemSearch(visible = true,
        onCreate = None,
        onSelectItem = action,
        showFirst = $.state.editedItem.previousLocation
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

      PhotosEditor(zoomL(itemPhotos) of $, initiallyOpen = $.props.initialItem.hasInvalidId),

      item.location match {
        case Some(location) =>
          div("Location:", putLocationElement, button("Remove", onClick --> remove),
            ItemListItem(location, modificationTime = 0, onClick = { _ => Callback.empty.asAsyncCallback }))
        case None =>
          div("Location:", putLocationElement)
      },

      div(DefaultEditor(value = item.description.asHtml, onChange = { event =>
        $.modState(itemDescription.replace(RichText.html(event.target.value))).runNow()
      })
      (className := "item-description")),

      CodesEditor(zoomL(itemCodes) of $),
    )
  }

  def initialState(props: Props): State = {
    val processedItem =
      Item.description.andThen(RichText.htmlLens)
        .modify(html => ProcessHtml.mapUrls(html, browserUrl))
        .apply(props.initialItem)
    logger.debug(props.initialItem.description.asHtml)
    logger.debug(processedItem.description.asHtml)
    State(editedItem = processedItem)
  }

  private val logger = log4s.getLogger

  val Component: Component[Props, State, Unit, CtorType.Props] =
    ScalaComponent.builder[Props]
      .initialStateFromProps(initialState)
      .render(render(_))
      .build
}
