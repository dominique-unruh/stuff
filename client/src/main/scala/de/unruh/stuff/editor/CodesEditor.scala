package de.unruh.stuff.editor

import de.unruh.stuff.{CodeButton, ItemSearch, ModalAction, QrCode}
import de.unruh.stuff.shared.Code
import japgolly.scalajs.react.callback.AsyncCallback
import japgolly.scalajs.react.component.Scala.{Component, Unmounted}
import japgolly.scalajs.react.extra.StateSnapshot
import japgolly.scalajs.react.vdom.VdomElement
import japgolly.scalajs.react.vdom.all.{button, className, div, onClick}
import japgolly.scalajs.react.{Callback, CtorType, ScalaComponent}
import japgolly.scalajs.react.vdom.Implicits._

object CodesEditor {
  case class Props(codes: StateSnapshot[Seq[Code]])

  def apply(codes: StateSnapshot[Seq[Code]]): Unmounted[Props, Unit, Unit] =
    Component(Props(codes = codes))

  private def addCode(code: Code)(implicit props: Props): Callback =
    props.codes.modState(_.appended(code))

  private def addCodeElement(implicit props: Props): VdomElement = ModalAction[Code](
    key = "add-code",
    onAction = addCode(_).asAsyncCallback,
    button = { (put: Callback) => button("Add code", onClick --> put): VdomElement },
    modal = { (action: Code => AsyncCallback[Unit]) =>
      QrCode(
        onDetect = { (f, c) => action(Code(f, c)) },
        constraints = ItemSearch.videoConstraints,
        active = true,
      ): VdomElement
    })

  private def removeCode(code: Code)(implicit props: Props): Callback =
    props.codes.modState(_.filterNot(_ == code))

  val Component: Component[Props, Unit, Unit, CtorType.Props] = ScalaComponent.builder[Props]
    .stateless
    .render_P { implicit props =>
      if (props.codes.value.nonEmpty) {
        val codes = props.codes.value.map(CodeButton(_, link = false, onRemove = Some(removeCode)): VdomElement)
        div(className := "item-codes")(codes.appended(addCodeElement): _*)
      } else
        addCodeElement
    }
    .build
}
