package de.unruh.stuff

import de.unruh.stuff.shared.Code
import japgolly.scalajs.react.component.Scala.{Component, Unmounted}
import japgolly.scalajs.react.vdom.all.{button, href, li, onClick}
import japgolly.scalajs.react.vdom.{TagMod, VdomElement}
import japgolly.scalajs.react.{CtorType, ScalaComponent}

object CodeButton {
  case class Props(code: Code, link: Boolean, onRemove: Option[Code => Callback])

  def apply(code: Code, link: Boolean, onRemove: Option[Code => Callback] = None): Unmounted[Props, Unit, Unit] =
    Component(Props(code = code, link = link, onRemove = onRemove))

  def render(props: Props): VdomElement = {
    val url = if (props.link) props.code.link else None
    val remove = props.onRemove match {
      case None => TagMod.empty
      case Some(onRemove) =>
        button("X", onClick --> onRemove(props.code))
    }
    url match {
      case None => li(props.code.toString, remove)
      case Some(url) => li(a(props.code.toString, href := url.toString))
    }
  }

  val Component: Component[Props, Unit, Unit, CtorType.Props] = ScalaComponent.builder[Props]
    .stateless
    .render_P(render)
    .build
}
