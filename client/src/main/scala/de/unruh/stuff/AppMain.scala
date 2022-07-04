package de.unruh.stuff

import de.unruh.stuff.editor.ItemEditor
import de.unruh.stuff.notistack.{OptionsObject, SnackbarProvider, VariantType}
import de.unruh.stuff.shared.{Code, Item, Utils}
import io.kinoplan.scalajs.react.material.ui.core.MuiInput
import japgolly.scalajs.react.Ref.Simple
import japgolly.scalajs.react.{CtorType, Ref, ScalaComponent}
import japgolly.scalajs.react.callback.{Callback, CallbackTo}
import japgolly.scalajs.react.component.Scala.{BackendScope, Component}
import japgolly.scalajs.react.component.builder.Lifecycle.ShouldComponentUpdate
import japgolly.scalajs.react.extra.router.SetRouteVia.{HistoryPush, HistoryReplace}
import japgolly.scalajs.react.extra.router.{BaseUrl, Path, ResolutionWithProps, Router, RouterConfigDsl, RouterWithProps, RouterWithPropsConfig, RouterWithPropsConfigDsl, SetRouteVia}
import japgolly.scalajs.react.vdom.Attr.ValueType
import japgolly.scalajs.react.vdom.all.{button, div, h1, key, onClick, style, untypedRef}
import org.scalajs.dom.{console, document, html, window}
import japgolly.scalajs.react.vdom.Implicits._
import japgolly.scalajs.react.vdom.{TagOf, VdomElement}
import monocle.{Lens, Optional, PLens, Prism}
import monocle.macros.Lenses
import org.log4s
import org.scalajs.dom.html.Div

import java.net.URI
import scala.collection.immutable
import scala.scalajs.js
import scala.scalajs.js.URIUtils.{decodeURIComponent, encodeURIComponent}
import scala.scalajs.js.{Date, UndefOr}
import scala.scalajs.js.annotation.JSExportTopLevel
import scala.util.Random

object StatePreservingRouter {
  /** How many pages to hold in memory */
  val maxPages = 20

  @Lenses
  case class RoutedView(page: AppMain.Page, component: VdomElement, hiddenComponent: VdomElement,
                        scrollPosition: Double, lastShown: Double)
  case class Props(page: AppMain.Page, component: VdomElement, hiddenComponent: VdomElement)
  @Lenses
  case class State(history: Map[Int, RoutedView] = Map(), current: Int = -1)
  object State {
    val currentHistory: Optional[State, RoutedView] = Optional[State, RoutedView] {
      state => state.history.get(state.current) } {
      view => state => if (state.current == -1) state else state.copy(history = state.history.updated(state.current, view))
    }
  }

  def evictOldest(history: Map[Int, RoutedView]): Map[Int, RoutedView] =
    if (history.sizeCompare(maxPages) > 0) {
      val oldest = history.values.minBy(_.lastShown).lastShown
      history.filter(_._2.lastShown > oldest)
    } else
      history

  private val logger = log4s.getLogger

  private def addPage(props: Props, state: State): State  = {
    // Save current scroll position
    val state2 = State.currentHistory.andThen(RoutedView.scrollPosition).modify(_ => window.scrollY).apply(state)
    val newRoutedView = RoutedView(props.page, props.component, props.hiddenComponent, scrollPosition = 0, lastShown = Date.now())

    // Try to find and replace old history entry. (While maintaining old scroll position)
    for ((idx,routedView) <- state2.history)
      if (routedView.page == newRoutedView.page)
        return state2.copy(history = state2.history.updated(idx, newRoutedView.copy(scrollPosition = routedView.scrollPosition)), current = idx)

    val newId = Utils.uniqueId()
    val newHistory = evictOldest(state2.history + (newId -> newRoutedView))
    logger.debug(s"New page($newId): ${newRoutedView.page}")
    state2.copy(newHistory, current = newId)
  }

  private def shouldComponentUpdate(currentState: State, nextState: State): CallbackTo[Boolean] = CallbackTo {
    currentState.current != nextState.current
  }

  private val displayNone = style := js.Dynamic.literal(display = "none")

  class Backend(bs: BackendScope[Props, State]) {
    def render(state: State): VdomElement = {
      for (current <- State.currentHistory.getOption(state)) {
        console.log("Scroll to ", current.scrollPosition.toInt)
        window.setTimeout(() => window.scrollTo(0, current.scrollPosition.toInt), 0)
//        window.scrollTo(0, current.scrollPosition.toInt)
      }
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

  def errorMessage(message: String): Callback =
    for (_ <- Callback { console.error(message) };
         snackbarProvider <- getSnackbarProvider)
    yield snackbarProvider.handleEnqueueSnackbar(message, OptionsObject(variant=VariantType.error))

  sealed trait Page
  case object Search extends Page
  case class ItemView(id: Item.Id) extends Page
  /** Every [[ItemCreate]] instance must have a `code` or a `uniqueId` (not both).
   * `uniqueId` is used to avoid that a (partially filled) page currently in the history is reopened
   * when a new item is to be created.
   **/
  case class ItemCreate private (code: Option[Code] = None, uniqueId: Option[Int] = None) extends Page {
    assert(code.nonEmpty || uniqueId.nonEmpty)
    assert(code.isEmpty || uniqueId.isEmpty)
  }
  object ItemCreate {
    def makeUnique(): ItemCreate = ItemCreate(Random.nextInt(Int.MaxValue))
    private def apply(code: Option[Code] = None, uniqueId: Option[Int] = None) : ItemCreate =
      throw new UnsupportedOperationException
    def apply(code: Code): ItemCreate = new ItemCreate(code = Some(code))
    def apply(uniqueId: Int): ItemCreate = new ItemCreate(uniqueId = Some(uniqueId))
  }

  /* The type [[Boolean]] of the router props is to contain a prop that is passed to the contained pages
  * (in a call to `res.renderP`) whether the page is visible or not. It is not used by the router itself, and the
  * prop passed to the router upon construction is ignored. */
  val routerConfig: RouterWithPropsConfig[Page, Boolean] = RouterWithPropsConfigDsl[Page, Boolean].buildConfig { dsl =>
    import dsl._

    val redirectRoot = staticRedirect(root) ~> redirectToPage(Search)(HistoryReplace)

    val search = staticRoute("#search", Search) ~> renderRP((ctl,visible) => ItemSearch(
      onSelectItem = { item => ctl.set(ItemView(item)).asAsyncCallback },
      onCreate = Some {
        case None => ctl.set(ItemCreate.makeUnique())
        case Some(code) => ctl.set(ItemCreate(code)) },
      visible = visible))

    val item = dynamicRouteCT("#item" / long.caseClass[ItemView]) ~> { page:ItemView =>
      renderR(ctl => ItemViewer(itemId = page.id, onSelectItem = { item => ctl.set(ItemView(item)) })) }

    def renderCreate(page: ItemCreate) = renderR( ctl => ItemEditor(Item.create(page.code),
      onSave = { item => ctl.set(ItemView(item.id), HistoryReplace) }, // replace by view
      onCancel = ctl.set(Search, HistoryPush) , // go back
    ))

    /** Taken from [[RouterConfigDsl.queryToSeq]]. */
    def decode(str: String): String =
      decodeURIComponent(str.replace('+', ' '))

    /** Modified from [[RouterConfigDsl.queryToSeq]]. */
    val needingEncoding = """%[0-9A-F][0-9A-F]""".r

    /** Modified from [[RouterConfigDsl.queryToSeq]]. */
    def encode(str: String): String =
      needingEncoding.replaceAllIn(encodeURIComponent(str), m =>
        m.group(0) match {
          case "%20" => "+"
          case "%00" => (0: Char).toString
          case "%3A" => ":"
          case s => s
        }
      )

    val remainingPathEncoded = remainingPath.xmap(decode)(encode)

    val remainingPathCode = remainingPathEncoded.pmap { str:String => Some(ItemCreate(Code(str))) }
                                                      { page => page.code.get.toString }
    val intCreateId = int.pmap { int:Int => Some(ItemCreate(int)) }
                               { page => page.uniqueId.get }

    val createEmpty = staticRedirect("#create") ~> redirectToPage(ItemCreate.makeUnique())(HistoryReplace)
    val create = dynamicRoute[ItemCreate]("#create" / intCreateId)
      { case ItemCreate(None, Some(id)) => ItemCreate(id) } ~> renderCreate
    val createCode = dynamicRoute[ItemCreate]("#createcode" / remainingPathCode)
      { case ItemCreate(Some(code), None) => ItemCreate(code) } ~> renderCreate

    ( redirectRoot | search | item | createEmpty | create | createCode )
      .notFound(redirectToPage(Search)(HistoryReplace))
  }
//    .logToConsole
    .renderWith { (ctl, res) =>
      StatePreservingRouter.routerRender(res.page,
        CallbackTo { res.renderP(true) }, CallbackTo { res.renderP(false) }) }

  private val baseUrl = BaseUrl.until_#

  @JSExportTopLevel("appMain")
  def appMain(): Unit = {
    val root = document.getElementById("react-root")
    val router = RouterWithProps(baseUrl, routerConfig).withProps(true)()
    val snackbarProvider = SnackbarProvider()(router, untypedRef := snackbarProviderRef.asInstanceOf[Ref.Simple[html.Element]])
    snackbarProvider.renderIntoDOM(root)
  }

  @JSExportTopLevel("test")
  def test(): Unit = {
    val snackbarProviderRef = Ref[SnackbarProvider]
    val app : VdomElement = button("Test", onClick --> Callback {
      val sp: SnackbarProvider = snackbarProviderRef.get.runNow().get
      sp.handleEnqueueSnackbar("Hello", OptionsObject(variant = VariantType.error))
    })
    val snackbarProvider = SnackbarProvider()(app, untypedRef := snackbarProviderRef.asInstanceOf[Ref.Simple[html.Element]])
    val root = document.getElementById("react-root")
    snackbarProvider.renderIntoDOM(root)
//    HTMLViewer().renderIntoDOM(root)
  }

  private val logger = log4s.getLogger

  def urlToPage(url: String): Option[Page] = {
    logger.debug(s"${baseUrl.value} $url")
    if (!url.startsWith(baseUrl.value))
      None
    else {
      val path = Path(url.stripPrefix(baseUrl.value))
      val parsed = routerConfig.rules.parse(path).runNow()
      val result = parsed.map(Some(_)).getOrElse(None)
      logger.debug(s"Result: $result")
      result
    }
  }

  def pageToUrl(page: Page): URI = {
    val path = routerConfig.rules.path(page)
    new URI(path.abs(baseUrl).value)
  }
}
