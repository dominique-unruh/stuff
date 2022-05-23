package controllers

import javax.inject._

import de.unruh.stuff.shared.SharedMessages
import play.api.mvc._

@Singleton
class Application @Inject()(cc: ControllerComponents) extends AbstractController(cc) with Authenticated {

  def index = Action { implicit request =>
    Ok(views.html.index(SharedMessages.itWorks))
  }
}
