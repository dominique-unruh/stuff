package de.unruh.stuff

import de.unruh.stuff.shared.Item
import de.unruh.stuff.shared.Utils
import io.kinoplan.scalajs.react.material.ui.core.MuiInput
import japgolly.scalajs.react.{CtorType, ScalaComponent}
import japgolly.scalajs.react.callback.{Callback, CallbackTo}
import japgolly.scalajs.react.component.Scala.{BackendScope, Component}
import japgolly.scalajs.react.component.builder.Lifecycle.ShouldComponentUpdate
import japgolly.scalajs.react.extra.router.SetRouteVia.HistoryReplace
import japgolly.scalajs.react.extra.router.{BaseUrl, Router, RouterConfigDsl, RouterWithPropsConfig}
import japgolly.scalajs.react.vdom.all.{div, h1, key, style}
import org.scalajs.dom.{console, document}
import japgolly.scalajs.react.vdom.Implicits._
import japgolly.scalajs.react.vdom.{TagOf, VdomElement}
import org.scalajs.dom.html.Div

import scala.collection.immutable
import scala.scalajs.js
import scala.scalajs.js.annotation.JSExportTopLevel

// TODO preserve scroll state
// TODO evict old pages at some point
// TODO disable camera
object StatePreservingRouter {
  case class RoutedView(page: AppMain.Page, component: VdomElement)
  case class Props(page: AppMain.Page, component: VdomElement)
  case class State(history: Map[Int, RoutedView] = Map(), current: Int = -1)

  private def addPage(props: Props, state: State): State  = {
    val newRoutedView = RoutedView(props.page, props.component)

    for ((idx,routedView) <- state.history)
      if (routedView.page == newRoutedView.page)
        return state.copy(history = state.history.updated(idx, newRoutedView), current = idx)

    val newId = Utils.uniqueId()
    state.copy(state.history + (newId -> newRoutedView), current = newId)
  }

  private def shouldComponentUpdate(currentState: State, nextState: State): CallbackTo[Boolean] = CallbackTo {
    currentState.current != nextState.current
  }

  private val displayNone = style := js.Dynamic.literal(display = "none")

  class Backend(bs: BackendScope[Props, State]) {
    def render(state: State): VdomElement = {
      val components: immutable.Iterable[VdomElement] = for ((idx,routeView) <- state.history)
        yield {
          val show = idx==state.current
          div(key := idx, routeView.component, displayNone.unless(show) )
        }
      div(components.toSeq :_*)
    }
  }

  val Component: Component[Props, State, Backend, CtorType.Props] = ScalaComponent.builder[Props]
    .initialState(State())
    .renderBackend[Backend]
    .getDerivedStateFromProps(addPage _)
    .shouldComponentUpdate[CallbackTo](should => shouldComponentUpdate(should.currentState, should.nextState))
    .build

  def routerRender(page: AppMain.Page, component: CallbackTo[VdomElement]): VdomElement =
    Component(Props(page, component.runNow()))
}

object AppMain {
  sealed trait Page
  case object Search extends Page
  case class ItemView(id: Item.Id) extends Page

  val routerConfig: RouterWithPropsConfig[Page, Unit] = RouterConfigDsl[Page].buildConfig { dsl =>
    import dsl._

    val redirectRoot = staticRedirect(root) ~> redirectToPage(Search)(HistoryReplace)
    val search = staticRoute("#search", Search) ~> renderR(ctl => ItemSearch(onClick = { item => ctl.set(ItemView(item)).runNow() }))
    val item = dynamicRouteCT("#item" / long.caseClass[ItemView]) ~> { page:ItemView => render(ItemViewer(page.id)) }

    ( redirectRoot | search | item )
      .notFound(redirectToPage(Search)(HistoryReplace))
  }
//    .logToConsole
    .renderWith((ctl, res) => StatePreservingRouter.routerRender(res.page, CallbackTo { res.render() }))

  @JSExportTopLevel("appMain")
  def appMain(): Unit = {
    val root = document.getElementById("react-root")
    val baseUrl = BaseUrl.until_#
    val router = Router(baseUrl, routerConfig)
    router().renderIntoDOM(root)
  }

  @JSExportTopLevel("test")
  def test(): Unit = {
    val root = document.getElementById("react-root")
    Camera(onPhoto = {photo => Callback {console.log("Photo:", photo)}},
      open = true,
      onClose = Callback("Camera should be closed")
    )
      .renderIntoDOM(root)
//    HTMLViewer().renderIntoDOM(root)
  }
}
