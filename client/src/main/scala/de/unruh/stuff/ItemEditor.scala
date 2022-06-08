package de.unruh.stuff

import de.unruh.stuff.reactsimplewysiwyg.DefaultEditor
import de.unruh.stuff.shared.{Item, RichText}
import io.kinoplan.scalajs.react.material.ui.core.{MuiDialog, MuiInput, ReactHandler2}
import japgolly.scalajs.react.callback.AsyncCallback
import japgolly.scalajs.react.component.Scala.{Component, Unmounted}
import japgolly.scalajs.react.vdom.all.{button, className, div, h1, href, img, li, onChange, onClick, src, textarea, value}
import japgolly.scalajs.react.vdom.{TagMod, VdomElement, all}
import japgolly.scalajs.react.{BackendScope, Callback, CtorType, ReactEvent, ReactFormEventFromInput, ScalaComponent}
import monocle.macros.Lenses

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import japgolly.scalajs.react.vdom.Implicits._
import org.scalajs.dom.console

import java.net.URI
import scala.util.{Failure, Success}

object ItemEditor {
  case class Props(initialItem: Item, onSave: Item => Callback, onCancel: Callback)

  @Lenses case class State(editedItem: Item,
                          /** Searching for a location to insert */
                           searching: Boolean = false,
                          )

  def apply(props: Props): Unmounted[Props, State, Backend] = Component(props)

  def apply(initialItem: Item, onSave: Item => Callback, onCancel: Callback): Unmounted[Props, State, Backend] =
    apply(Props(initialItem = initialItem, onSave = onSave, onCancel = onCancel))

  private val itemName = State.editedItem.andThen(Item.name)
  private val itemDescription = State.editedItem.andThen(Item.description)
  private val itemPhotos = State.editedItem.andThen(Item.photos)

  private def url(url: URI) = ExtendedURL.resolve(JSVariables.username, url)

  class Backend(bs: BackendScope[Props, State]) {
    private val save: AsyncCallback[Unit] = {
      for (state <- bs.state.asAsyncCallback;
           props <- bs.props.asAsyncCallback;
           result <- DbCache.updateOrCreateItemReact(state.editedItem).attemptTry;
           _ <- (result match {
             case Success(id) =>
               for (item <- AsyncCallback.fromFuture(DbCache.getItem(id)); // Important to use the id returned from server. It might be fresh if we edited a new item
                    // This reloads the item. Important because the server processes the item in .updateItems
                    _ <- bs.modState(state => state.copy(editedItem = item)).asAsyncCallback;
                    _ <- AppMain.successMessage("Saved").asAsyncCallback;
                    _ <- props.onSave(item).asAsyncCallback)
                 yield {}
             case Failure(exception) =>
               AppMain.errorMessage("Failed to save item", exception).asAsyncCallback
           })
           )
      yield {}
    }

    /** Call the onCancel callback (when user wants to cancel) */
    private val cancel = for (props <- bs.props; _ <- props.onCancel) yield {}

    private def cameraOnPhoto(photo: String) : Callback = {
      val url = URI.create(photo)
      assert(url.getScheme == "data")
      val modify = itemPhotos.modify(_.appended(url))
      bs.modState(modify)
    }

    /** Activate search to insert a location */
    private val put : Callback =
      bs.modState(_.copy(searching = true))

    private val endSearch : Callback =
      bs.modState(_.copy(searching = false))

    /** Sets a new location and hides the search dialog */
    private def setLocation(id: Item.Id): Callback =
      for (state <- bs.state;
           newItem = state.editedItem.setLocation(id);
           _ <- bs.setState(state.copy(editedItem = newItem, searching = false)))
        yield {}

    private val remove : Callback =
      bs.modState(state => state.copy(state.editedItem.clearLocation()))

    def render(props: Props, state: State): VdomElement = {
      val item = state.editedItem
      div(className := "item-editor state-editing",
        div(all.button("Save", onClick --> save), all.button("Cancel", onClick --> cancel)),
        MuiInput(
          //          inputProps = js.Dynamic.literal(`type`="search"),
          /*inputProps = MuiInputProps(ariaLabel = "Description")*/
        )(
          className := "item-name",
          onChange ==> { event: ReactFormEventFromInput => bs.modState(itemName.replace(event.target.value)) },
          value := item.name
        ),

        // TODO allow removing pictures
        if (item.photos.nonEmpty) {
          val images = for (photo <- item.photos)
            yield (img(src := url(photo)): VdomElement)
          div(className := "item-photos")(images: _*)
        } else
          TagMod.empty,

        ModalAction[String](
          button = { (open:Callback) => div(button(onClick --> open)("Add photo")) : VdomElement },
          modal = { (onPhoto:String=>Callback) => Camera(onPhoto = onPhoto) : VdomElement },
          onAction = cameraOnPhoto _
        ),

        item.location match {
          case Some(location) =>
            div("Location:", button("Put", onClick --> put), button("Remove", onClick --> remove),
              ItemListItem(location, onClick = { _ => Callback.empty }))
          case None =>
            div("Location:", button("Put", onClick --> put))
        },

        MuiDialog(open = state.searching,
          onClose = { (e,s) => endSearch } : ReactHandler2[ReactEvent, String]) (
          // TODO: don't show create actions
          // TODO: in search results, show prevLocation first
          ItemSearch(visible = true,
            onCreate = { _ => AppMain.errorMessage("Creating items is not possible from this search") },
            onSelectItem = setLocation,
          )
        ),

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
