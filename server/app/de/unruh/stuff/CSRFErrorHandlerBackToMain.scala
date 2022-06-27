package de.unruh.stuff
import play.api.mvc.Results.Unauthorized
import play.api.mvc.{RequestHeader, Result}

import scala.concurrent.Future

class CSRFErrorHandlerBackToMain extends play.filters.csrf.CSRF.ErrorHandler {
  override def handle(req: RequestHeader, msg: String): Future[Result] =
    Future.successful(Unauthorized(views.html.csrf()))
}
