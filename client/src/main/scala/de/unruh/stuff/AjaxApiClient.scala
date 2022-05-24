package de.unruh.stuff

import org.scalajs.dom
import org.scalajs.dom.{BodyInit, Headers, HttpMethod, RequestInit, Response, console, webcrypto}
import ujson.{Js, Value}

import scala.collection.IterableOnce.iterableOnceExtensionMethods
import scala.collection.mutable
import scala.concurrent.Future
import scala.scalajs.js.UndefOr
import scala.scalajs.js.Thenable.Implicits.thenable2future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js
import scala.scalajs.js.annotation.JSGlobal

object AjaxApiClient extends autowire.Client[ujson.Value, upickle.default.Reader, upickle.default.Writer]{
  @js.native
  @JSGlobal
  private val csrf_token : String = js.native

  def write[Result: upickle.default.Writer](r: Result): Value = upickle.default.writeJs(r)
  def read[Result: upickle.default.Reader](p: ujson.Value): Result = upickle.default.read[Result](p)

  override def doCall(req: Request): Future[ujson.Value] = {
    val header = new Headers()
    header.set("Csrf-Token", csrf_token)
    header.set("Content-Type", "application/json")
    val request = new RequestInit {
      method = HttpMethod.POST
      headers = header
      body = ujson.Obj(mutable.LinkedHashMap.from(req.args)).render()
    }
    val url = "/api/" + req.path.mkString("/")
    for (response <- dom.fetch(url, request);
         text <- response.text)
      yield ujson.read(text)
  }
}
