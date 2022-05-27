package de.unruh.stuff

import autowire.clientCallable
import de.unruh.stuff.shared.{AjaxApi, Item}
import de.unruh.stuff.shared.Item.Id

import scala.collection.mutable
import scala.concurrent.Future
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

object DbCache {
  def getItem(id: Id): Future[Item] = {
    val counter = updateCounter
    cache.get(id) match {
      case Some(item) => Future.successful(item)
      case None =>
        for (item <- AjaxApiClient[AjaxApi].getItem(id).call();
             _ = if (updateCounter == counter) cache.put(id, item);
             item2 <- getItem(id))
          yield item2
    }
  }

  def updateItem(item: Item) : Future[Unit] = {
    ???
    /* TODO:
       store on server via AjaxApiClient[AjaxApi].putItem(id).call()
       upon completion, set locally  (cache.put)
       upon failure, invalidate locally  (cache.remove)
       in both cases, updateCounter += 1
       pass completion/failure through to return value future
     */
  }

  private val cache = mutable.HashMap[Item.Id, Item]()
  /** Increase this when updating the DB so that pending Ajax calls will be ignored. */
  private var updateCounter = 0
}
