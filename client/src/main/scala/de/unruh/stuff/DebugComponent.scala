package de.unruh.stuff

import japgolly.scalajs.react.{CtorType, ScalaComponent}
import japgolly.scalajs.react.component.Scala.{Component, Unmounted}
import japgolly.scalajs.react.vdom.all.span
import japgolly.scalajs.react.vdom.Implicits._

import scala.scalajs.js.Date

object DebugComponent {
  val Component: Component[Any, Unit, Unit, CtorType.Props] = ScalaComponent.builder[Any]
    .render_P(props => span(s"$props @ ${new Date().toLocaleTimeString()}"))
    .build
  def apply(v: Any): Unmounted[Any, Unit, Unit] = DebugComponent.Component(v)
}