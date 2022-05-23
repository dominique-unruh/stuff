package de.unruh.stuff

import de.unruh.stuff.shared.SharedMessages
import org.scalajs.dom
import org.scalajs.dom.{console, document}
import slinky.core.{Component, CustomAttribute, CustomTag, ExternalComponent, ExternalComponentNoProps, ExternalComponentWithAttributes, WithAttrs}
import slinky.core.annotations.react
import slinky.core.facade.ReactElement
import slinky.web.ReactDOM
import slinky.web.html.{`type`, div, h1, input, onChange, value}

import scala.scalajs.js
import scala.scalajs.js.annotation.{JSExportTopLevel, JSImport}
import scala.scalajs.js.{UndefOr, |}
import scala.scalajs.js.|._


@JSImport("@" +
  "mui/material", JSImport.Default)
@js.native
object MaterialUi extends js.Object {
  val AppBar: js.Object                  = js.native
  val Avatar: js.Object                  = js.native
  val Backdrop: js.Object                = js.native
  val Badge: js.Object                   = js.native
  val BottomNavigation: js.Object        = js.native
  val BottomNavigationAction: js.Object  = js.native
  val Button: js.Object                  = js.native
  val ButtonBase: js.Object              = js.native
  val Card: js.Object                    = js.native
  val CardActions: js.Object             = js.native
  val CardContent: js.Object             = js.native
  val CardHeader: js.Object              = js.native
  val CardMedia: js.Object               = js.native
  val Checkbox: js.Object                = js.native
  val Chip: js.Object                    = js.native
  val CircularProgress: js.Object        = js.native
  val ClickAwayListener: js.Object       = js.native
  val Collapse: js.Object                = js.native
  val Dialog: js.Object                  = js.native
  val DialogActions: js.Object           = js.native
  val DialogContent: js.Object           = js.native
  val DialogContentText: js.Object       = js.native
  val DialogTitle: js.Object             = js.native
  val Divider: js.Object                 = js.native
  val Drawer: js.Object                  = js.native
  val ExpansionPanel: js.Object          = js.native
  val ExpansionPanelActions: js.Object   = js.native
  val ExpansionPanelDetails: js.Object   = js.native
  val ExpansionPanelSummary: js.Object   = js.native
  val Fade: js.Object                    = js.native
  val FormControl: js.Object             = js.native
  val FormControlLabel: js.Object        = js.native
  val FormGroup: js.Object               = js.native
  val FormHelperText: js.Object          = js.native
  val FormLabel: js.Object               = js.native
  val Grid: js.Object                    = js.native
  val GridList: js.Object                = js.native
  val GridListTile: js.Object            = js.native
  val GridListTileBar: js.Object         = js.native
  val Grow: js.Object                    = js.native
  val Hidden: js.Object                  = js.native
  val Icon: js.Object                    = js.native
  val IconButton: js.Object              = js.native
  val Input: js.Object                   = js.native
  val InputAdornment: js.Object          = js.native
  val InputLabel: js.Object              = js.native
  val LinearProgress: js.Object          = js.native
  val List: js.Object                    = js.native
  val ListItem: js.Object                = js.native
  val ListItemAvatar: js.Object          = js.native
  val ListItemIcon: js.Object            = js.native
  val ListItemSecondaryAction: js.Object = js.native
  val ListItemText: js.Object            = js.native
  val ListSubheader: js.Object           = js.native
  val Menu: js.Object                    = js.native
  val MenuItem: js.Object                = js.native
  val MenuList: js.Object                = js.native
  val MobileStepper: js.Object           = js.native
  val Modal: js.Object                   = js.native
  val MuiThemeProvider: js.Object        = js.native
  val Paper: js.Object                   = js.native
  val Popover: js.Object                 = js.native
  val Portal: js.Object                  = js.native
  val Radio: js.Object                   = js.native
  val RadioGroup: js.Object              = js.native
  val Reboot: js.Object                  = js.native
  val Select: js.Object                  = js.native
  val Slide: js.Object                   = js.native
  val Snackbar: js.Object                = js.native
  val SnackbarContent: js.Object         = js.native
  val Step: js.Object                    = js.native
  val StepButton: js.Object              = js.native
  val StepContent: js.Object             = js.native
  val StepIcon: js.Object                = js.native
  val StepLabel: js.Object               = js.native
  val Stepper: js.Object                 = js.native
  val SvgIcon: js.Object                 = js.native
  val Switch: js.Object                  = js.native
  val Tab: js.Object                     = js.native
  val Table: js.Object                   = js.native
  val TableBody: js.Object               = js.native
  val TableCell: js.Object               = js.native
  val TableFooter: js.Object             = js.native
  val TableHead: js.Object               = js.native
  val TablePagination: js.Object         = js.native
  val TableRow: js.Object                = js.native
  val TableSortLabel: js.Object          = js.native
  val Tabs: js.Object                    = js.native
  val TextField: js.Object               = js.native
  val Toolbar: js.Object                 = js.native
  val Tooltip: js.Object                 = js.native
  val Typography: js.Object              = js.native
  val Zoom: js.Object                    = js.native
}


@react object Button extends ExternalComponentWithAttributes[slinky.web.html.button.tag.type] {
  case class Props(
//                    color: UndefOr[color] = js.undefined,
                   className: UndefOr[String] = js.undefined,
//                   variant: UndefOr[variant] = js.undefined,
                   disabled: UndefOr[Boolean] = js.undefined,
                   disableFocusRipple: UndefOr[Boolean] = js.undefined,
                   fullWidth: UndefOr[Boolean] = js.undefined,
                   href: UndefOr[String] = js.undefined,
                   mini: UndefOr[Boolean] = js.undefined,
                   onClick: UndefOr[slinky.core.SyntheticEvent[Button.this.type#RefType, org.scalajs.dom.Event] => Unit] = js.undefined,
//                   size: UndefOr[size] = js.undefined,
                  )
  override val component = MaterialUi.Button

}

/*@react class Badge extends Component {
  type Props = Unit
  case class State(number: Int)

  override def initialState: State = State(0)

  val badge = CustomTag("Badge")
  val mailIcon = CustomTag("MailIcon")
  val badgeContent = CustomAttribute[Int]("badgeContent")

  override def render(): ReactElement =
    badge(badgeContent := state.number)(mailIcon())
}*/


/*@react class Badge extends ExternalComponent {
  type Props = Unit
  case class State(number: Int)

  override def initialState: State = State(0)

  val badge = CustomTag("Badge")
  val mailIcon = CustomTag("MailIcon")
  val badgeContent = CustomAttribute[Int]("badgeContent")

  override def render(): ReactElement =
    badge(badgeContent := state.number)(mailIcon())
}*/

@react class TestComponent extends Component {
  case class Props(message: String)
  case class State(text: String)

  override def initialState: State = State("")

  val test: WithAttrs[div.tag.type] = div()

  override def render(): ReactElement = div (
    h1(props.message + " " + state.text),
    input (
      `type` := "text",
      value := state.text,
      onChange := { e => setState(state.copy(e.target.value)); console.log(e.target.value) }
    )
  )
}

object ScalaJSExample {
  @JSExportTopLevel("test")
  def test(username: String): Unit = {
    val testComponent = TestComponent(s"Hello ${username}!")
    def action(): Unit = console.log("Click")
    val button = Button(Button.Props(onClick = { _:Any => action() }))("hello")
    ReactDOM.render(
      div (
        testComponent,
        button,
      ),
      document.getElementById("react-root")
    )
  }
}
