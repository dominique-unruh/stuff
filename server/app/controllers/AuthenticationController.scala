package controllers

import AuthenticationController.{HTTP_TRANSPORT, JSON_FACTORY, credentialForm}
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.HttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.gson.GsonFactory
import controllers.AssetsFinder
import de.unruh.stuff.Config
import play.api.Configuration
import play.api.data.Form
import play.api.data.Forms.{mapping, text}
import play.api.mvc.Security.Authenticated
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents, EssentialAction, Request, RequestHeader, Result, Results}

import javax.inject._
import scala.jdk.CollectionConverters.{CollectionHasAsScala, SeqHasAsJava}

@Singleton
class AuthenticationController @Inject()(val controllerComponents: ControllerComponents,
                                         implicit val assetsFinder: AssetsFinder,
                                         implicit val config: Configuration) extends BaseController {
  def login(): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.login(credentialForm))
  }

  def logout(): Action[AnyContent] = Action {
    Ok(views.html.loggedout()).withNewSession
  }

  def authenticate(): Action[AnyContent] = Action { implicit request =>
    val form = credentialForm.bindFromRequest()
    assert(!form.hasErrors)
    val formData = form.value.get


    val verifier = new GoogleIdTokenVerifier.Builder(HTTP_TRANSPORT, JSON_FACTORY)
      .setAudience(Seq(Config.googleClientId).asJava)
      .build();

    verifier.verify(formData.credential) match {
      case null => Unauthorized("Invalid ID token (authentication failed).")
      case idToken =>
        val payload = idToken.getPayload
        val user = payload.getEmail
        assert(user != null)

        if (Config.users.contains(user))
          Redirect(routes.Application.app).withSession("user" -> user)
        else
          Unauthorized(s"Unknown user $user")
    }
  }
}

object AuthenticationController {
  import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
  import com.google.api.client.http.HttpTransport

  val JSON_FACTORY: JsonFactory = GsonFactory.getDefaultInstance
  var HTTP_TRANSPORT: HttpTransport = GoogleNetHttpTransport.newTrustedTransport

  case class GoogleCredentials(clientId: String, credential: String)
  val credentialForm: Form[GoogleCredentials] = Form(mapping("clientId" -> text, "credential" -> text)(GoogleCredentials.apply)(GoogleCredentials.unapply))
}

/** Provides authentication related functions to controllers that inherit from this trait. */
trait Authenticated extends BaseController {
  private def usernameOption(request: RequestHeader): Option[String] = request.session.get("user")
  private def onUnauthorized(request: RequestHeader): Result = Results.Redirect(routes.AuthenticationController.login())
  def isAuthenticated(action: Action[_]): EssentialAction =
    Authenticated(usernameOption, onUnauthorized) { user => action }
  def isAuthenticated(f: => Request[AnyContent] => Result): EssentialAction =
      isAuthenticated(Action(request => f(request)))
  def username(implicit request: Request[_]) : String =
    usernameOption(request).getOrElse(throw new IllegalStateException("username invoked in unauthenticated context"))
}