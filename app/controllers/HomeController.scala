package controllers

import javax.inject._
import play.api.{mvc, _}
import play.api.data.Form
import play.api.data.Forms.{mapping, text}
import play.api.mvc._
import HomeController._
import play.api.ApplicationLoader.Context
import play.api.http.Writeable
import play.api.libs.json.Json
import play.api.mvc.Security.Authenticated

import java.util.Optional


/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(val controllerComponents: ControllerComponents,
                               implicit val assetsFinder: AssetsFinder) extends BaseController with Authenticated {

  def index() = isAuthenticated { implicit request: Request[AnyContent] =>
    Ok(views.html.index(username))
  }
}

object HomeController {
}
