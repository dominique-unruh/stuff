package de.unruh.stuff

import autowire.clientCallable
import de.unruh.stuff.shared.{AjaxApi, Item}
import de.unruh.stuff.shared.Item.{INVALID_ID, Id}
import japgolly.scalajs.react.callback.AsyncCallback

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

  private implicit class FinallyFuture[A](val future: Future[A]) extends AnyVal {
    def finalmente(code: => Unit): Future[A] = future.transform { result => code; result }
  }

  def updateItem(item: Item) : Future[Unit] = {
    assert(item.id != INVALID_ID)
    for (_ <- AjaxApiClient[AjaxApi].updateItem(item).call()
                  .finalmente { updateCounter += 1; cache.remove(item.id) };
         // Removing the item from the cache because the server may process it in .updateItem
         _ = cache.remove(item.id))
      yield ()
  }

  def updateItemReact(item: Item) : AsyncCallback[Unit] =
    AsyncCallback.fromFuture(updateItem(item))

  private val cache = mutable.HashMap[Item.Id, Item]()
  /** Increase this when updating the DB so that pending Ajax calls will be ignored. */
  private var updateCounter = 0
}
