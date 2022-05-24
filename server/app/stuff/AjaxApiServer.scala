package stuff

import de.unruh.stuff.db.Yaml
import de.unruh.stuff.shared.{AjaxApi, Item}
import de.unruh.stuff.shared.Item.Id
import play.api.libs.json.JsValue
import play.twirl.api.TemplateMagic.anyToDefault
import ujson.play.PlayJson

import java.nio.file.Path
import scala.concurrent.ExecutionContext.Implicits.global

object AjaxApiImpl extends AjaxApi {
  // TODO: Return only IDs
  // TODO: truncate to reasonable number of results
  // TODO: most recent first
  override def search(searchString: String): Seq[Item] = {
    // TODO don't reload DB each time
    val db = Yaml.loadDb(Path.of("example"))
    db.values.filter(_.matches(searchString)).toSeq
  }
}

object AjaxApiServer extends autowire.Server[JsValue, upickle.default.Reader, upickle.default.Writer] {
  def write[Result: upickle.default.Writer](r: Result): JsValue = upickle.default.transform(r).to(PlayJson)
  def read[Result: upickle.default.Reader](p: JsValue): Result = PlayJson.transform(p, upickle.default.reader[Result])
  val routes: AjaxApiServer.Router = AjaxApiServer.route[AjaxApi](AjaxApiImpl)
}

