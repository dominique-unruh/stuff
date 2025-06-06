package de.unruh.stuff

import org.scalajs.dom
import org.scalajs.dom.{BodyInit, Headers, HttpMethod, RequestInit, Response, console, webcrypto}
import ujson.{Js, Value}

import java.io.IOException
import scala.collection.mutable
import scala.concurrent.Future
import scala.scalajs.js.Thenable.Implicits.thenable2future
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

object AjaxApiClient extends autowire.Client[ujson.Value, upickle.default.Reader, upickle.default.Writer]{
  def write[Result: upickle.default.Writer](r: Result): Value = upickle.default.writeJs(r)
  def read[Result: upickle.default.Reader](p: ujson.Value): Result = upickle.default.read[Result](p)

  override def doCall(req: Request): Future[ujson.Value] = {
    val header = new Headers()
    header.set("Csrf-Token", JSVariables.csrf_token)
    header.set("Content-Type", "application/json")
    val request = new RequestInit {
      method = HttpMethod.POST
      headers = header
      body = ujson.Obj(upickle.core.LinkedHashMap.apply(req.args)).render()
    }
    val url = JSVariables.url_path_prefix + "/api/" + req.path.mkString("/")
    for (response <- dom.fetch(url, request);
         _ = if (!response.ok) throw new IOException(s"Ajax call failed ($url): ${response.statusText}");
         text <- response.text)
      yield ujson.read(text)
  }
}
