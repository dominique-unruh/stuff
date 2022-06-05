package de.unruh.stuff

import de.unruh.stuff.notistack.{OptionsObject, SnackbarProvider, VariantType}
import de.unruh.stuff.shared.{Code, Item, Utils}
import io.kinoplan.scalajs.react.material.ui.core.MuiInput
import japgolly.scalajs.react.Ref.Simple
import japgolly.scalajs.react.{CtorType, Ref, ScalaComponent}
import japgolly.scalajs.react.callback.{Callback, CallbackTo}
import japgolly.scalajs.react.component.Scala.{BackendScope, Component}
import japgolly.scalajs.react.component.builder.Lifecycle.ShouldComponentUpdate
import japgolly.scalajs.react.extra.router.SetRouteVia.{HistoryPush, HistoryReplace}
import japgolly.scalajs.react.extra.router.{BaseUrl, ResolutionWithProps, Router, RouterConfigDsl, RouterWithProps, RouterWithPropsConfig, RouterWithPropsConfigDsl, SetRouteVia}
import japgolly.scalajs.react.vdom.Attr.ValueType
import japgolly.scalajs.react.vdom.all.{button, div, h1, key, onClick, style, untypedRef}
import org.scalajs.dom.{console, document, html}
import japgolly.scalajs.react.vdom.Implicits._
import japgolly.scalajs.react.vdom.{TagOf, VdomElement}
import org.scalajs.dom.html.Div

import scala.collection.immutable
import scala.scalajs.js
import scala.scalajs.js.UndefOr
import scala.scalajs.js.annotation.JSExportTopLevel

// TODO preserve scroll state
// TODO evict old pages at some point
// TODO disable camera
object StatePreservingRouter {
  case class RoutedView(page: AppMain.Page, component: VdomElement, hiddenComponent: VdomElement)
  case class Props(page: AppMain.Page, component: VdomElement, hiddenComponent: VdomElement)
  case class State(history: Map[Int, RoutedView] = Map(), current: Int = -1)

  private def addPage(props: Props, state: State): State  = {
    val newRoutedView = RoutedView(props.page, props.component, props.hiddenComponent)

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
          div(key := idx, if (show) routeView.component else routeView.hiddenComponent, displayNone.unless(show) )
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

  def routerRender(page: AppMain.Page, component: CallbackTo[VdomElement]): VdomElement = {
    val component2 = component.runNow()
    Component(Props(page, component2, component2))
  }

  def routerRender(page: AppMain.Page, component: CallbackTo[VdomElement], hiddenComponent: CallbackTo[VdomElement]): VdomElement = {
    Component(Props(page, component.runNow(), hiddenComponent.runNow()))
  }
}

object AppMain {
  private val snackbarProviderRef: Ref.Simple[SnackbarProvider] = Ref[SnackbarProvider]
  private def getSnackbarProvider: CallbackTo[SnackbarProvider] =
    for (snackbar <- snackbarProviderRef.get)
      yield snackbar.getOrElse { throw new IllegalStateException("SnackbarProvider reference is empty") }

  def successMessage(message: String): Callback =
    for (snackbarProvider <- getSnackbarProvider)
      yield snackbarProvider.handleEnqueueSnackbar(message, OptionsObject(variant=VariantType.success))

  def errorMessage(message: String, exception: Throwable): Callback =
    for (_ <- Callback { console.error(message, exception) };
         snackbarProvider <- getSnackbarProvider)
      yield snackbarProvider.handleEnqueueSnackbar(message, OptionsObject(variant=VariantType.error))

  sealed trait Page
  case object Search extends Page
  case class ItemView(id: Item.Id) extends Page
  case class ItemCreate(code: Option[Code] = None) extends Page
  object ItemCreate {
    def apply(code: Code): ItemCreate = ItemCreate(Some(code))
  }

  val routerConfig: RouterWithPropsConfig[Page, Boolean] = RouterWithPropsConfigDsl[Page, Boolean].buildConfig { dsl =>
    import dsl._

    val redirectRoot = staticRedirect(root) ~> redirectToPage(Search)(HistoryReplace)

    val search = staticRoute("#search", Search) ~> renderRP((ctl,visible) => ItemSearch(
      onClick = { item => ctl.set(ItemView(item)) },
      onCreate = {
        case None => ctl.set(ItemCreate())
        case Some(code) => ctl.set(ItemCreate(code)) },
      visible = visible))

    val item = dynamicRouteCT("#item" / long.caseClass[ItemView]) ~> { page:ItemView => render(ItemViewer(page.id)) }

/*
    val renderCreate = dynRenderR[ItemCreate, VdomElement]( (page,ctl) => ItemEditor(Item.create(page.code),
      onSave = { item => ctl.set(ItemView(item.id), HistoryReplace) }, // replace by view
      onCancel = ctl.set(Search, HistoryPush) , // go back
    ))
*/

    def renderCreate(page: ItemCreate) = renderR( ctl => ItemEditor(Item.create(page.code),
      onSave = { item => ctl.set(ItemView(item.id), HistoryReplace) }, // replace by view
      onCancel = ctl.set(Search, HistoryPush) , // go back
    ))

    val remainingPathCode = remainingPath.pmap { str:String => Some(ItemCreate(Code(str))) }
                                               { page => page.code.get.toString }

    val create = staticRoute("#create", ItemCreate()) ~> renderCreate(ItemCreate())
    val createCode = dynamicRoute[ItemCreate]("#create" / remainingPathCode)
      { case ItemCreate(code : Some[Code]) => ItemCreate(code) } ~> renderCreate

    ( redirectRoot | search | item | create | createCode )
      .notFound(redirectToPage(Search)(HistoryReplace))
  }
//    .logToConsole
    .renderWith((ctl, res) => StatePreservingRouter.routerRender(res.page,
      CallbackTo { res.renderP(true) }, CallbackTo { res.renderP(false) }))

  @JSExportTopLevel("appMain")
  def appMain(): Unit = {
    val root = document.getElementById("react-root")
    val baseUrl = BaseUrl.until_#
    val router = RouterWithProps(baseUrl, routerConfig).withProps(true)()
    val snackbarProvider = SnackbarProvider()(router, untypedRef := snackbarProviderRef.asInstanceOf[Ref.Simple[html.Element]])
    snackbarProvider.renderIntoDOM(root)
  }

  @JSExportTopLevel("test")
  def test(): Unit = {
    val snackbarProviderRef = Ref[SnackbarProvider]
    val app : VdomElement = button("Test", onClick --> Callback {
      val sp: SnackbarProvider = snackbarProviderRef.get.runNow().get
      console.log(sp)
      sp.handleEnqueueSnackbar("Hello", OptionsObject(variant = VariantType.error))
      console.log("Click")
    })
    val snackbarProvider = SnackbarProvider()(app, untypedRef := snackbarProviderRef.asInstanceOf[Ref.Simple[html.Element]])
    val root = document.getElementById("react-root")
    snackbarProvider.renderIntoDOM(root)
//    HTMLViewer().renderIntoDOM(root)
  }
}
