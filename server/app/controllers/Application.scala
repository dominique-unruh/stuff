package controllers

import de.unruh.stuff.{AjaxApiServer, ExtendedURL, Paths}

import javax.inject._
import de.unruh.stuff.shared.SharedMessages
import play.api.http.MimeTypes
import play.api.libs.json.{JsObject, Json}
import play.api.mvc._
import play.filters.csrf.AddCSRFToken

import java.nio.file.Files
import scala.concurrent.Await
import scala.concurrent.duration.Duration

@Singleton
class Application @Inject()(cc: ControllerComponents) extends AbstractController(cc) with Authenticated {
  def app: Handler = isAuthenticated { implicit request =>
    Ok(views.html.app(username))
  }

  def ajaxApi(method: String): Handler = isAuthenticated { implicit request =>
    val apiRequest = autowire.Core.Request(
      method.split('/').toSeq,
      request.body.asJson.get
        .asInstanceOf[JsObject].value.toMap.map{case (k,v) => (k, v)}
    )
    val resultFuture = AjaxApiServer.routes.apply(apiRequest)
    val resultJson = Await.result(resultFuture, Duration.Inf)
    val resultString = Json.stringify(resultJson)
    Ok(resultString).as(MimeTypes.JSON)
  }

  def file(user: String, id: Long, filename: String): Handler = isAuthenticated { implicit request =>
    assert(user == username)
    assert(ExtendedURL.fileRegex.matches(filename))
    val path = Paths.filesPath.resolve(id.toString).resolve(filename)
    assert(Files.isRegularFile(path))
    Ok(Files.readAllBytes(path)).withHeaders("Cache-Control" -> "max-age=604800, immutable")
  }
}
