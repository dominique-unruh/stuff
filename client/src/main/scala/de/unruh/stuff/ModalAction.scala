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
                      modal: (A => AsyncCallback[Unit]) => VdomElement,
                      initiallyOpen: Boolean)
  case class State(open: Boolean)

  def apply[A](/** React key of this component */
               key: String,
               /** Callback to invoke when the modal takes an "action" */
               onAction: A => AsyncCallback[Unit],
               /** Element that shows the modal upon user action (e.g., a button).
                * Gets a [[Callback]] that shows the modal.  */
               button: Callback => VdomElement,
               /** Content of the modal. Gets a callback to perform the "action". */
               modal: (A => AsyncCallback[Unit]) => VdomElement,
               /** Show this modal upon first rendering */
               initiallyOpen: Boolean = false): Unmounted[Props[_], State, Backend] =
    Component.withKey(key)(Props[A](onAction=onAction, button=button, modal=modal, initiallyOpen=initiallyOpen))

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

  val Component: Component[Props[_], State, Backend, CtorType.Props] =
    ScalaComponent.builder[Props[_]]
      .initialStateFromProps { props => State(open=props.initiallyOpen) }
      .renderBackend[Backend]
      .build
}

