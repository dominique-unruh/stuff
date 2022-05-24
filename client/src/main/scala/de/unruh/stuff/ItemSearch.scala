package de.unruh.stuff

import de.unruh.stuff.shared.Item
import org.scalajs.dom.{Event, console}
import slinky.core.{Component, SyntheticEvent}
import slinky.core.annotations.react
import slinky.core.facade.{React, ReactElement}
import slinky.web.html.div

@react class ItemSearch extends Component {
  case class Props(items: Seq[Item], onClick: Item => Unit = { _ => () })
  override type State = Seq[Item]

  override def initialState : State = props.items

  private def changed(event: SyntheticEvent[TextField.RefType, Event]) : Unit = {
    val searchTerms : String = event.target.value
    setState(props.items.filter(_.matches(searchTerms)))
  }

  override def render(): ReactElement = div (
    TextField(TextField.Props(fullWidth = true, placeholder = "Search", variant = TextField.FILLED,
      autoFocus = true, onChange = changed _)),
    ItemList(state, props.onClick)
  )
}
