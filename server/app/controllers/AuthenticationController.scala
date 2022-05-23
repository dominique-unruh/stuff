package controllers

import AuthenticationController.loginForm
import controllers.AssetsFinder
import play.api.data.Form
import play.api.data.Forms.{mapping, text}
import play.api.mvc.Security.Authenticated
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents, EssentialAction, Request, RequestHeader, Result, Results}

import javax.inject._

@Singleton
class AuthenticationController @Inject()(val controllerComponents: ControllerComponents,
                                         implicit val assetsFinder: AssetsFinder) extends BaseController {
  def login(): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.login(loginForm))
  }

  def logout(): Action[AnyContent] = Action {
    Ok(views.html.loggedout()).withNewSession
  }

  def authenticate(): Action[AnyContent] = Action { implicit request =>
    val form = loginForm.bindFromRequest()
    assert(!form.hasErrors)
    val login = form.value.get
    assert(login.user == "unruh")
    assert(login.password == "secret")
    Redirect(routes.Application.app).withSession("user" -> login.user)
  }
}

object AuthenticationController {
  case class Login(user: String, password: String)
  val loginForm: Form[Login] = Form(mapping("user" -> text, "password" -> text)(Login.apply)(Login.unapply))
}

/** Provides authentication related functions to controllers that inherit from this trait. */
trait Authenticated extends BaseController {
  private def usernameOption(request: RequestHeader): Option[String] = request.session.get("user")
  private def onUnauthorized(request: RequestHeader): Result = Results.Redirect(routes.AuthenticationController.login())
  def isAuthenticated(f: => Request[AnyContent] => Result): EssentialAction =
    Authenticated(usernameOption, onUnauthorized) { user =>
      Action(request => f(request))
    }
  def username(implicit request: Request[_]) : String =
    usernameOption(request).getOrElse(throw new IllegalStateException("username invoked in unauthenticated context"))
}