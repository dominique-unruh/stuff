package de.unruh.stuff

import io.kinoplan.scalajs.react.material.ui.core.{MuiDialog, ReactHandler2}
import japgolly.scalajs.react.callback.AsyncCallback
import japgolly.scalajs.react.{Callback, CtorType, ReactEvent, ScalaComponent}
import japgolly.scalajs.react.component.Scala.{BackendScope, Component, Unmounted}
import japgolly.scalajs.react.vdom.VdomElement
import japgolly.scalajs.react.vdom.all.{button, div, onClick, span}

object ModalAction {
  case class Props[A](onAction: A => AsyncCallback[Unit],
                      button: Callback => VdomElement,
                      modal: (A => AsyncCallback[Unit]) => VdomElement)
  case class State(open: Boolean = false)

  private def apply[A](props: Props[A]): Unmounted[Props[_], State, Backend] = Component(props)
  def apply[A](onAction: A => AsyncCallback[Unit],
               button: Callback => VdomElement,
               modal: (A => AsyncCallback[Unit]) => VdomElement): Unmounted[Props[_], State, Backend] =
    apply[A](Props[A](onAction=onAction, button=button, modal=modal))

  class Backend(bs: BackendScope[Props[_], State]) {
    private val close : Callback =
      bs.modState(_.copy(open=false))

    private val open : Callback =
      bs.modState(_.copy(open=true))

    private def action[A](props: Props[A], value: A): AsyncCallback[Unit] =
      for (_ <- close.asAsyncCallback;
           _ <- props.onAction(value))
      yield {}

    def render[A](props: Props[A], state: State): VdomElement =
        span (props.button (open),
          MuiDialog(open = state.open, onClose = { (e, s) => close }: ReactHandler2[ReactEvent, String])
          ( // This will show as a modal popup when "open = true"
            props.modal(v => action(props, v))
          ))

  }

  val Component: Component[Props[_], State, Backend, CtorType.Props] = ScalaComponent.builder[Props[_]]
    .initialState(State())
    .renderBackend[Backend]
    .build
}

