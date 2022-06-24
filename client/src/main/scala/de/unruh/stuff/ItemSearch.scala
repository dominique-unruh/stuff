package de.unruh.stuff

import autowire.clientCallable
import de.unruh.stuff.shared.Item.Id
import de.unruh.stuff.shared.{AjaxApi, Code, Item, Utils}
import io.kinoplan.scalajs.react.material.ui.core.MuiInput
import japgolly.scalajs.react.callback.Callback
import japgolly.scalajs.react.component.Scala.{Component, Unmounted}
import japgolly.scalajs.react.component.builder.Lifecycle
import japgolly.scalajs.react.component.builder.Lifecycle.RenderScope
import japgolly.scalajs.react.util.DefaultEffects
import japgolly.scalajs.react.vdom.{TagMod, VdomElement, VdomNode, all}
import japgolly.scalajs.react.vdom.all.{autoFocus, button, className, div, h1, onChange, onClick, placeholder, value}
import japgolly.scalajs.react.vdom.Implicits._
import japgolly.scalajs.react.{AsyncCallback, BackendScope, CtorType, React, ReactFormEventFromInput, ScalaComponent}
import org.log4s
import org.scalajs.dom.{MediaTrackConstraints, console}

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js

// TODO: Allow to activate/deactivate camera
// TODO: Automatically deactivate camera after inactivity
object ItemSearch {
  /** How many results to load */
  val numResults = 100

  case class Props(onSelectItem: Item.Id => AsyncCallback[Unit], onCreate: Option[Option[Code] => Callback], visible: Boolean)
  case class ResultProps(searchString: String = "", searchFromCode: Option[Code], parent: Props)
  object ResultProps {
    def apply(props: Props, state: State): ResultProps =
      new ResultProps(searchString = state.searchString, searchFromCode = state.searchFromCode, parent = props)
  }
  case class State(
                  /** User input search string */
                  searchString: String = "",
                  /** User input: flashlight on? */
                  flashLight: Boolean = false,
                  /** Current search initiated by QR code reader? `None` means no, `Some(code)` means yes. */
                  searchFromCode: Option[Code] = None,
                  )

  val videoConstraints: MediaTrackConstraints = new MediaTrackConstraints {
    aspectRatio = 1
    facingMode = "environment"
  }

  def apply(props: Props): Unmounted[Props, State, Unit] = Component(props)
  def apply(onSelectItem: Item.Id => AsyncCallback[Unit], onCreate: Option[Option[Code] => Callback], visible: Boolean): Unmounted[Props, State, Unit] =
    Component(Props(onSelectItem=onSelectItem, onCreate = onCreate, visible = visible))

  private def loadAndRenderResults(implicit $: Lifecycle.RenderScope[ResultProps, Unit, Unit]) : AsyncCallback[VdomElement] =
    for (results <- AsyncCallback.fromFuture(AjaxApiClient[AjaxApi].search($.props.searchString, numResults).call()))
      yield
        renderResults(results)

  def renderResults(results : Seq[(Item.Id, Long)])(implicit $: Lifecycle.RenderScope[ResultProps, Unit, Unit]) : VdomElement = {
    if (results.isEmpty) {
      div(
        // TODO Nicer formatting (https://mui.com/material-ui/react-alert/ ?)
        h1("Nothing found", className := "no-search-results"),
        $.props.searchFromCode match {
          case Some(code) if $.props.parent.onCreate.nonEmpty =>
            div(button(s"Create new item with code $code?", onClick --> $.props.parent.onCreate.get(Some(code))))
          case _ => TagMod.empty
        })
    } else div(
//      button("test", onClick --> Callback { console.log($.props.searchString) }),
      ItemList(results, $.props.parent.onSelectItem))
  }

  private def changed(event: ReactFormEventFromInput)(implicit $: RenderScope[Props, State, Unit]) =
    $.modState(_.copy(searchString = event.target.value, searchFromCode = None))

  private def qrcode(format: Option[String], text: String)(implicit $: RenderScope[Props, State, Unit]) : Callback = {
    val state = $.state
    val code = Code(format, text)
    val searchString = s"code:${js.URIUtils.encodeURIComponent(code.toString)} "
    val needToAdd = state.searchString != searchString
    if (needToAdd) {
      $.modState(_.copy(
        searchString = searchString,
        searchFromCode = Some(code)))
    } else
      DefaultEffects.Sync.empty
  }

  private def onError(error: Throwable): AsyncCallback[VdomElement] =
    AsyncCallback.delay {
      error.printStackTrace()
      // TODO Nicer formatting (e.g., https://mui.com/material-ui/react-alert/)
      h1("Failed to load results", className := "search-failed")
    }

  def render(implicit $: Lifecycle.RenderScope[Props, State, Unit]): VdomNode = {
    implicit val state: State = $.state
    implicit val props: Props = $.props
    div (className := "item-search") (
      QrCode(onDetect = qrcode(_,_).asAsyncCallback, constraints = videoConstraints, flashLight = state.flashLight, active = props.visible)/*.withRef(qrCodeRef)*/,
      div(
        button(onClick --> $.modState(_.copy(flashLight = true)), "Flashlight"),
        " ",
        props.onCreate match { case Some(onCreate) => button(onClick --> onCreate(None), "New"); case None => TagMod.empty }
      ),
      MuiInput(inputProps = js.Dynamic.literal(`type`="search"))
      (className := "item-search-input", onChange ==> changed,
        placeholder := "Search...", autoFocus := true,
        value := state.searchString),
      ResultComponent(ResultProps(props, state)),
    )
  }

  private val logger = log4s.getLogger

  private val initialState = State()

  private val ResultComponent = ScalaComponent.builder[ResultProps]
    .stateless
    .render { $ =>
      console.log("Rerender")
      React.Suspense(
        // TODO: Use React 18 stuff (useTransition/startTransition) to keep the old data in view, only blurred
        fallback = h1("Loading...", className := "state-waiting"),
        asyncBody = loadAndRenderResults($).handleError(onError)
      )
    }
    .shouldComponentUpdatePure { upd =>
      upd.currentProps.searchString != upd.nextProps.searchString ||
      upd.currentProps.searchFromCode != upd.nextProps.searchFromCode }
    .build

  //noinspection TypeAnnotation
  val Component = ScalaComponent.builder[Props]
    .initialState(initialState)
    .render(render(_))
    .build
}
