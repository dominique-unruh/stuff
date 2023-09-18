package de.unruh.stuff

import de.unruh.stuff.Search.SearchSpecAnd
import de.unruh.stuff.shared.{Code, Item}

import java.net.{URI, URL, URLDecoder}
import java.nio.charset.Charset
import scala.util.matching.Regex

object Search {
  def search(db: Map[Item.Id, Item], searchString: String): Seq[Item] =
    search(db, processSearchString(searchString))

  def search(db: Map[Item.Id, Item], searchSpec: SearchSpec): Seq[Item] =
    db.values.filter(searchSpec.doesMatch).toSeq

  private def processSearchTerm(searchTerm: String, finished: Boolean): SearchSpec = {
    if (searchTerm.startsWith("code:"))
      SearchSpecCode(Code(URLDecoder.decode(searchTerm.stripPrefix("code:"), "utf-8")))
    else if (searchTerm.startsWith("location:"))
      SearchSpecLocation(searchTerm.stripPrefix("location:").toLong)
    else
      SearchSpecWord(searchTerm, !finished)
  }

  private val processSearchStringSplitRegex = "\\s+".r
  def processSearchString(searchString: String): SearchSpec = {
    if (searchString.isBlank) return SearchSpecAll
    val terms = processSearchStringSplitRegex.split(searchString).iterator.filter(_.nonEmpty).toList
    assert(terms.nonEmpty)
    val finalBlank = searchString.last.isWhitespace
    // pairs (search-term, finished)
    val specs = terms.dropRight(1).map(processSearchTerm(_,finished = true)).appended(processSearchTerm(terms.last,finalBlank))
    SearchSpecAnd(specs:_*)
  }

  trait SearchSpec {
    def doesMatch(item: Item) : Boolean
  }
  case class SearchSpecAnd(conditions: SearchSpec*) extends SearchSpec {
    def doesMatch(item: Item): Boolean = conditions.forall(_.doesMatch(item))
  }
  case object SearchSpecAll extends SearchSpec {
    override def doesMatch(item: Item): Boolean = true
  }
  case class SearchSpecWord(word: String, prefix: Boolean) extends SearchSpec {
    private val re = raw"(?i)\b${Regex.quote(word)}${if (prefix) "" else "\\b"}".r
    override def doesMatch(item: Item): Boolean = {
      re.findFirstIn(item.name).nonEmpty ||
      re.findFirstIn(item.description.asHtml).nonEmpty
    }
  }
  case class SearchSpecCode(code: Code) extends SearchSpec {
    override def doesMatch(item: Item): Boolean =
      item.codes.exists(code.matches)
  }

  case class SearchSpecLocation(id: Item.Id) extends SearchSpec {
    override def doesMatch(item: Item): Boolean =
      item.location.contains(id)
  }
}
