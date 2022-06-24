package de.unruh.stuff.shared

trait AjaxApi {
  /** Returns the first `numResults` matches in the item database.
   * Most recently modified first.
   * @param numResults show at most this many results
   * @param showFirst show this item first in the search results (if it is part of the results)
   * @return pairs of id and the modification time */
  def search(searchString: String, numResults: Int, showFirst: Option[Item.Id]) : Seq[(Item.Id, Long)]
  def getItem(id: Item.Id) : Item
  /** Updates this item. `item.id` must refer to an already existing item.
   * @return new `lastModified` value. */
  def updateItem(item: Item) : Long
  /** Adds this item to the database. `item.id` must be [[Item.INVALID_ID]].
   * The id will be chosen by the server.
   * @return (chosen id, lastModified) */
  def createItem(item: Item) : (Item.Id, Long)
  /** Sets the last modified time of the item (not made persistent) */
  def touchLastModified(id: Item.Id) : Unit
  /** Removed the location from the item.
   * @return new `lastModified` */
  def clearLocation(id: Item.Id): Long
  /** Sets the location from the item.
   * @return new `lastModified` */
  def setLocation(id: Item.Id, locationId: Item.Id): Long
}

