package de.unruh.stuff

import de.unruh.stuff.shared.Item
import japgolly.scalajs.react.extra.router.SetRouteVia.HistoryReplace
import japgolly.scalajs.react.extra.router.{BaseUrl, Router, RouterConfigDsl, RouterWithPropsConfig}
import japgolly.scalajs.react.vdom.all.h1
import org.scalajs.dom.{console, document}
import japgolly.scalajs.react.vdom.Implicits._

import scala.scalajs.js.annotation.JSExportTopLevel

object AppMain {
  sealed trait Page
  case object Search extends Page
  case class ItemView(id: Item.Id) extends Page

  val routerConfig: RouterWithPropsConfig[Page, Unit] = RouterConfigDsl[Page].buildConfig { dsl =>
    import dsl._

    val redirectRoot = staticRedirect(root) ~> redirectToPage(Search)(HistoryReplace)
    val search = staticRoute("#search", Search) ~> renderR( ctl => ItemSearch(onClick = { item => ctl.set(ItemView(item)).runNow() }))
    val item = dynamicRouteCT("#item" / long.caseClass[ItemView]) ~> { page:ItemView => render(ItemEditor(page.id)) }

    ( redirectRoot | search | item )
      .notFound(redirectToPage(Search)(HistoryReplace))
//      .logToConsole
  }

  @JSExportTopLevel("appMain")
  def appMain(): Unit = {
    val root = document.getElementById("react-root")
    val baseUrl = BaseUrl.until_#
    val router = Router(baseUrl, routerConfig)
    router().renderIntoDOM(root)
  }
}
