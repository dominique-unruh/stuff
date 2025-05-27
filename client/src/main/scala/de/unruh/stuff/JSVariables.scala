package de.unruh.stuff

import scala.scalajs.js
import scala.scalajs.js.annotation.JSGlobal

/** Contains several variables that are explicitly set as part of the page header. */
object JSVariables {
  @js.native @JSGlobal("stuff_csrf_token")
  val csrf_token : String = js.native
  @js.native @JSGlobal("stuff_username")
  val username : String = js.native
  @js.native @JSGlobal("stuff_url_path_prefix")
  val url_path_prefix: String = js.native
}
