package de.unruh.stuff.shared

trait AjaxApi {
  /** Returns the first `numResults` matches in the item database.
   * Most recently modified first.
   * @return pairs of id and the modification time */
  def search(searchString: String, numResults: Int) : Seq[(Item.Id, Long)]
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
}

