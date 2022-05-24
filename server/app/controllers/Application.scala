package controllers

import de.unruh.stuff.ExtendedURL

import javax.inject._
import de.unruh.stuff.shared.SharedMessages
import play.api.libs.json.{JsObject, Json}
import play.api.mvc._
import play.filters.csrf.AddCSRFToken
import stuff.{AjaxApiServer, Paths}

import java.nio.file.Files
import scala.concurrent.Await
import scala.concurrent.duration.Duration

@Singleton
class Application @Inject()(cc: ControllerComponents) extends AbstractController(cc) with Authenticated {

  def index: Handler = Action { implicit request =>
    Ok(views.html.index(SharedMessages.itWorks))
  }

  def app: Handler = isAuthenticated { implicit request =>
    Ok(views.html.app(username))
  }

  def ajaxApi(method: String): Handler = isAuthenticated { implicit request =>
    val apiRequest = autowire.Core.Request(
      method.split('/'),
      request.body.asJson.get
        .asInstanceOf[JsObject].value.toMap.map{case (k,v) => (k, v)}
    )
    val resultFuture = AjaxApiServer.routes.apply(apiRequest)
    val resultJson = Await.result(resultFuture, Duration.Inf)
    val resultString = Json.stringify(resultJson)
    Ok(resultString)
  }

  def file(user: String, id: Long, filename: String): Handler = isAuthenticated { implicit request =>
    assert(user == username)
    assert(ExtendedURL.fileRegex.matches(filename))
    val path = Paths.filesPath.resolve(id.toString).resolve(filename)
    println(path)
    assert(Files.isRegularFile(path))
    Ok(Files.readAllBytes(path))
  }
}
