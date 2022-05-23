package controllers

import javax.inject._
import play.api.mvc._

@Singleton
class HomeController @Inject()(val controllerComponents: ControllerComponents,
                               implicit val assetsFinder: AssetsFinder) extends BaseController with Authenticated {

  def index(): EssentialAction = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.index())
  }
}
