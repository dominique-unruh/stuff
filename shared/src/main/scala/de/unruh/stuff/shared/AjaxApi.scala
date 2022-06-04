package de.unruh.stuff.shared

trait AjaxApi {
  /** Returns the first `numResults` matches in the item database.
   * Most recently modified first. */
  def search(searchString: String, numResults: Int) : Seq[Item.Id]
  def getItem(id: Item.Id) : Item
  def updateItem(item: Item) : Unit
}

