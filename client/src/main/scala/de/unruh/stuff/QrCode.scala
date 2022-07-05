package de.unruh.stuff

import japgolly.scalajs.react.component.Scala.Unmounted
import japgolly.scalajs.react.vdom.{VdomElement, all}
import japgolly.scalajs.react.vdom.all.{className, div, video}
import japgolly.scalajs.react.{AsyncCallback, BackendScope, Callback, ScalaComponent}
import org.scalajs.dom.{MediaStreamConstraints, MediaTrackConstraintSet, MediaTrackConstraints, console}

import scala.scalajs.js
import scala.scalajs.js.{UndefOr, |}
import scala.util.Random
import japgolly.scalajs.react.vdom.Implicits._
import org.log4s

// TODO: If opening camera fails, report to user
object QrCode {
  /** onDetect: (format, content) */
  // TODO class does not reinitialize when MediaTrackConstraints change. Should it?
  case class Props(onDetect: (Option[String], String) => AsyncCallback[Unit], constraints: MediaTrackConstraints,
                   flashLight: Boolean, active: Boolean)
  case class State(scanner: zxing.BrowserMultiFormatReader,
                   flashLightState: Boolean = false)

  def apply(props: Props): Unmounted[Props, State, Backend] = Component(props)
  def apply(onDetect: (Option[String], String) => AsyncCallback[Unit], constraints: MediaTrackConstraints, flashLight: Boolean = false,
           active: Boolean): Unmounted[Props, State, Backend] =
    apply(Props(onDetect=onDetect, constraints=constraints, flashLight=flashLight, active=active))

  def initialState: State = State(scanner = new zxing.BrowserMultiFormatReader())

  class Backend(bs: BackendScope[Props, State]) {
    private val videoId = s"qrcode-scanner-${Random.nextInt()}"

    def render(): VdomElement = div(video(all.id := videoId), className := "qrcode-scanner")

    private def callback(props: Props)(result: zxing.Result, exception: UndefOr[zxing.ZException]): Unit = {
      if (exception != null && exception.nonEmpty) {
        val exn = exception.get
        if (exn.isInstanceOf[zxing.NotFoundException])
          {}
        else
          logger.warn("Exception in ZXing: " + exception)
      } else {
        props.onDetect(zxing.BarcodeFormat.fromT(result.getBarcodeFormat()).map(_.toString), result.getText()).runNow()
      }
    }

    private val logger = log4s.getLogger

//    private val updateActive: Callback =

    /** Changes the flashLight state if necessary */
    private val updateFlashLight: Callback =
      for (state <- bs.state;
           props <- bs.props;
           shouldSet = state.scanner.stream != null && !js.isUndefined(state.scanner.stream) && state.flashLightState != props.flashLight;
           _ <- (if (shouldSet)
                   // We do this first, so that if the following fails (non-asynchronously), we don't keep trying
                     bs.setState(state.copy(flashLightState = props.flashLight))
                else Callback {}))
      yield
        if (shouldSet) {
          state.scanner.stream.getVideoTracks()(0).applyConstraints(
            new MediaTrackConstraints {
              advanced = js.Array(js.Dynamic.literal(torch = props.flashLight).asInstanceOf[MediaTrackConstraintSet])
            })
          ()
        }

    private def decodingStarted(): Unit = {
      updateFlashLight.runNow()
    }

    private def startScanning(props: Props, state: State) : Unit = {
      state.scanner.decodeFromConstraints(
        constraints = new MediaStreamConstraints { video = props.constraints },
        videoId, callback(props))
        .`then`( { _:Unit => decodingStarted() : Unit | scala.scalajs.js.Thenable[Unit] }, js.undefined)
    }

    private def stopScanning(props: Props, state: State) : Unit = {
      state.scanner.stopStreams()
    }

    val componentDidMount : Callback =
      for (props <- bs.props;
           state <- bs.state)
      yield startScanning(props, state)

    val componentWillUnmount: Callback = {
      for (state <- bs.state)
        yield try {
          state.scanner.reset()
        } catch {
          case error: Throwable => console.error("Failed to clear ZXing QR code scanner.", error);
        }
    }

    private def updateActive(prevProps: Props): Callback =
      for (props <- bs.props;
           state <- bs.state)
        yield
          if (prevProps.active && !props.active)
            stopScanning(props, state)
          else if (!prevProps.active && props.active)
            startScanning(props, state)

    def componentDidUpdate(prevProp: Props, presState: State): Callback =
      for (_ <- updateFlashLight;
           _ <- updateActive(prevProp))
        yield {}
  }

  //noinspection TypeAnnotation
  val Component = ScalaComponent.builder[Props]
    .initialState(initialState)
    .renderBackend[Backend]
    .componentDidMount(_.backend.componentDidMount)
    .componentWillUnmount(_.backend.componentWillUnmount)
    .componentDidUpdate(upd => upd.backend.componentDidUpdate(upd.prevProps, upd.prevState))
    .build
}
