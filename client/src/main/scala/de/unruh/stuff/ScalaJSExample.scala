package de.unruh.stuff

import de.unruh.stuff.shared.Item.testItems
import de.unruh.stuff.shared.{Item, SharedMessages}
import org.scalajs.dom
import org.scalajs.dom.{Event, HTMLInputElement, console, document}
import slinky.core.{Component, CustomAttribute, CustomTag, ExternalComponent, ExternalComponentNoProps, ExternalComponentWithAttributes, ExternalComponentWithRefType, SyntheticEvent, WithAttrs}
import slinky.core.annotations.react
import slinky.core.facade.ReactElement
import slinky.web.ReactDOM
import slinky.web.html.{`type`, div, h1, input, onChange, value}

import scala.scalajs.js
import scala.scalajs.js.annotation.{JSExportTopLevel, JSImport}
import scala.scalajs.js.{UndefOr, |}

@JSImport("@mui/icons-material", JSImport.Default)
@js.native
object IconsMaterial extends js.Object {
  val IceSkating: js.Object = js.native
}

class Icon(icon: js.Object) extends ExternalComponentNoProps {
  override val component: String | js.Object = icon
}
object Icon {
  def apply(icon: js.Object): Icon = new Icon(icon)
}

@JSImport("@mui/material", JSImport.Default)
@js.native
object MaterialUi extends js.Object {
//  val AppBar: js.Object                  = js.native
  val Avatar: js.Object                  = js.native
//  val Backdrop: js.Object                = js.native
//  val Badge: js.Object                   = js.native
//  val BottomNavigation: js.Object        = js.native
//  val BottomNavigationAction: js.Object  = js.native
  val Button: js.Object                  = js.native
//  val ButtonBase: js.Object              = js.native
  val Card: js.Object                    = js.native
//  val CardActions: js.Object             = js.native
  val CardContent: js.Object             = js.native
//  val CardHeader: js.Object              = js.native
  val CardMedia: js.Object               = js.native
//  val Checkbox: js.Object                = js.native
//  val Chip: js.Object                    = js.native
//  val CircularProgress: js.Object        = js.native
//  val ClickAwayListener: js.Object       = js.native
//  val Collapse: js.Object                = js.native
//  val Dialog: js.Object                  = js.native
//  val DialogActions: js.Object           = js.native
//  val DialogContent: js.Object           = js.native
//  val DialogContentText: js.Object       = js.native
//  val DialogTitle: js.Object             = js.native
//  val Divider: js.Object                 = js.native
//  val Drawer: js.Object                  = js.native
//  val ExpansionPanel: js.Object          = js.native
//  val ExpansionPanelActions: js.Object   = js.native
//  val ExpansionPanelDetails: js.Object   = js.native
//  val ExpansionPanelSummary: js.Object   = js.native
//  val Fade: js.Object                    = js.native
//  val FormControl: js.Object             = js.native
//  val FormControlLabel: js.Object        = js.native
//  val FormGroup: js.Object               = js.native
//  val FormHelperText: js.Object          = js.native
//  val FormLabel: js.Object               = js.native
//  val Grid: js.Object                    = js.native
//  val GridList: js.Object                = js.native
//  val GridListTile: js.Object            = js.native
//  val GridListTileBar: js.Object         = js.native
//  val Grow: js.Object                    = js.native
//  val Hidden: js.Object                  = js.native
//  val Icon: js.Object                    = js.native
//  val IconButton: js.Object              = js.native
//  val Input: js.Object                   = js.native
//  val InputAdornment: js.Object          = js.native
//  val InputLabel: js.Object              = js.native
//  val LinearProgress: js.Object          = js.native
  val List: js.Object                    = js.native
  val ListItemButton: js.Object                = js.native
  val ListItemAvatar: js.Object          = js.native
  val ListItemIcon: js.Object            = js.native
//  val ListItemSecondaryAction: js.Object = js.native
  val ListItemText: js.Object            = js.native
//  val ListSubheader: js.Object           = js.native
//  val Menu: js.Object                    = js.native
//  val MenuItem: js.Object                = js.native
//  val MenuList: js.Object                = js.native
//  val MobileStepper: js.Object           = js.native
//  val Modal: js.Object                   = js.native
//  val MuiThemeProvider: js.Object        = js.native
//  val Paper: js.Object                   = js.native
//  val Popover: js.Object                 = js.native
//  val Portal: js.Object                  = js.native
//  val Radio: js.Object                   = js.native
//  val RadioGroup: js.Object              = js.native
//  val Reboot: js.Object                  = js.native
//  val Select: js.Object                  = js.native
//  val Slide: js.Object                   = js.native
//  val Snackbar: js.Object                = js.native
//  val SnackbarContent: js.Object         = js.native
//  val Step: js.Object                    = js.native
//  val StepButton: js.Object              = js.native
//  val StepContent: js.Object             = js.native
//  val StepIcon: js.Object                = js.native
//  val StepLabel: js.Object               = js.native
//  val Stepper: js.Object                 = js.native
//  val SvgIcon: js.Object                 = js.native
//  val Switch: js.Object                  = js.native
//  val Tab: js.Object                     = js.native
//  val Table: js.Object                   = js.native
//  val TableBody: js.Object               = js.native
//  val TableCell: js.Object               = js.native
//  val TableFooter: js.Object             = js.native
//  val TableHead: js.Object               = js.native
//  val TablePagination: js.Object         = js.native
//  val TableRow: js.Object                = js.native
//  val TableSortLabel: js.Object          = js.native
//  val Tabs: js.Object                    = js.native
  val TextField: js.Object               = js.native
//  val Toolbar: js.Object                 = js.native
//  val Tooltip: js.Object                 = js.native
//  val Typography: js.Object              = js.native
//  val Zoom: js.Object                    = js.native
}


/*@react object Card extends ExternalComponent {
  case class Props(className: UndefOr[String] = js.undefined, raised: UndefOr[Boolean] = js.undefined)
  override val component = MaterialUi.Card
}

@react object CardContent extends ExternalComponent {
  case class Props(component: UndefOr[String | js.Function] = js.undefined)
  override val component = MaterialUi.CardContent
}

@react object CardMedia extends ExternalComponent {
  case class Props(className: UndefOr[String] = js.undefined,
                   component: UndefOr[String | js.Function] = js.undefined,
                   image: UndefOr[String] = js.undefined,
                   height: UndefOr[Int] = js.undefined,
                   src: UndefOr[String] = js.undefined)
  override val component = MaterialUi.CardMedia
}*/

/*@react object Button extends ExternalComponentWithAttributes[slinky.web.html.button.tag.type] {
  case class Props(
                   onClick: UndefOr[slinky.core.SyntheticEvent[Button.this.type#RefType, org.scalajs.dom.Event] => Unit] = js.undefined,
                  )
  override val component = MaterialUi.Button
}*/

object List extends ExternalComponentNoProps {
  override val component: String | js.Object = MaterialUi.List
}

object ListItemButton extends ExternalComponent {
  case class Props(onClick: UndefOr[SyntheticEvent[ListItemButton.this.type#RefType, Event] => Unit])
  override val component: String | js.Object = MaterialUi.ListItemButton
}

object ListItemIcon extends ExternalComponentNoProps {
  override val component: String | js.Object = MaterialUi.ListItemIcon
}

object Avatar extends ExternalComponent {
  case class Props(src: UndefOr[String] = js.undefined, variant: UndefOr[String] = js.undefined)
  override val component: String | js.Object = MaterialUi.Avatar

  val SQUARE = "square"
}

object ListItemAvatar extends ExternalComponentNoProps {
  override val component: String | js.Object = MaterialUi.ListItemAvatar
}

object ListItemText extends ExternalComponent {
  case class Props(primary: UndefOr[ReactElement] = js.undefined, secondary: UndefOr[ReactElement] = js.undefined)
  override val component: String | js.Object = MaterialUi.ListItemText
}

object TextField extends ExternalComponentWithRefType[dom.HTMLInputElement] {
  case class Props(fullWidth: UndefOr[Boolean] = js.undefined,
                   placeholder: UndefOr[String] = js.undefined,
                   variant: UndefOr[String] = js.undefined,
                   autoFocus: UndefOr[Boolean] = js.undefined,
                   onChange: UndefOr[SyntheticEvent[TextField.this.type#RefType, Event] => Unit] = js.undefined)

  val FILLED = "filled"
  val OUTLINED = "outlined"

  override val component: String | js.Object = MaterialUi.TextField
}

object ScalaJSExample {
  @JSExportTopLevel("test")
  def test(username: String): Unit = {
    val itemList = ItemSearch(testItems,
      onClick = { item:Item => console.log(item) })
    ReactDOM.render(
      itemList,
      document.getElementById("react-root")
    )
  }
}
