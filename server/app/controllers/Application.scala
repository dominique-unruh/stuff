package controllers

import controllers.Application.qrSheetOptionsForm
import de.unruh.stuff.{AjaxApiServer, Config, ExtendedURL, Paths, QrSheet}

import javax.inject._
import play.api.data.Form
import play.api.data.Forms.{mapping, number, optional, text}
import play.api.http.MimeTypes
import play.api.libs.json.{JsObject, Json}
import play.api.mvc._

import java.nio.file.Files
import scala.concurrent.Await
import scala.concurrent.duration.Duration

@Singleton
class Application @Inject()(cc: ControllerComponents, implicit val config: Config) extends AbstractController(cc) with Authenticated {
  def app: Handler = isAuthenticated { implicit request =>
    Ok(views.html.app(username))
      .withHeaders("Cache-Control" -> "no-cache")
  }

  def test: Handler = Action { implicit request =>
    Ok(views.html.test(username))
  }

  val KB = 1024
  val MB: Int = KB * KB
  val ajaxMaxLength: Int = 10 * MB

  def ajaxApi(method: String): Handler = isAuthenticated (Action(parse.json(maxLength = ajaxMaxLength)) { implicit request =>
    val apiRequest = autowire.Core.Request(
      method.split('/').toSeq,
      request.body
        .asInstanceOf[JsObject].value.toMap.map{case (k,v) => (k, v)}
    )
    val resultFuture = AjaxApiServer.routes(username).apply(apiRequest)
    val resultJson = Await.result(resultFuture, Duration.Inf)
    val resultString = Json.stringify(resultJson)
    Ok(resultString).as(MimeTypes.JSON)
  })

  def file(user: String, id: Long, filename: String): Handler = isAuthenticated { implicit request =>
    assert(user == username)
    assert(ExtendedURL.fileRegex.matches(filename))
    val path = Paths.filePath(user,id,filename)
    assert(Files.isRegularFile(path))
    Ok(Files.readAllBytes(path)).withHeaders("Cache-Control" -> "max-age=604800, immutable")
  }

  def qrSheet() : Handler = Action { implicit request =>
    val form = qrSheetOptionsForm.bindFromRequest().value.getOrElse(QrSheet.SheetOptions())
    Ok(views.html.qrsheet(form))
  }

  def qrCode(content: String, size: Int) : Handler = Action { implicit request =>
//    val form = qrImageOptionsForm.bindFromRequest()
//    assert(!form.hasErrors)
    Ok(QrSheet.createQrCode(content, size))
      .withHeaders("Cache-Control" -> "max-age=604800, immutable")
      .as("image/png")
  }
}

object Application {
  val qrSheetOptionsForm: Form[QrSheet.SheetOptions] =
    Form(mapping("template" -> optional(text), "count" -> optional(number), "size" -> optional(number))
      (QrSheet.SheetOptions.apply)(QrSheet.SheetOptions.unapply))

/*  val qrImageOptionsForm: Form[QrSheet.ImageOptions] = Form(mapping("content" -> text, "size" -> number)
    (QrSheet.ImageOptions.apply)(QrSheet.ImageOptions.unapply))*/
}