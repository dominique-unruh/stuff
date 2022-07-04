package de.unruh.stuff

import de.unruh.stuff.shared.RichText
import org.scalajs.dom.{Element, HTMLAnchorElement, HTMLElement, console, document}

import java.net.URI

object ProcessHtml {
  def stringToDom(string: String): Element = {
    val span = document.createElement("span")
    span.innerHTML = string
    if (span.childNodes.lengthCompare(1) == 0)
      span.childNodes.item(0) match {
        case element: Element => element
        case _ => span
      }
    else
      span
  }

  /** Warning: modified the `element` tree in place */
  def mapUrlsDom(element: Element, f: URI => URI): Unit = {
    console.log(s"Entering: $element")
    element match {
      case anchor: HTMLAnchorElement =>
        val href = anchor.href
        if (href != null) {
          val newHref = f(new URI(href)).toString
          if (href != newHref) {
            anchor.href = newHref
            if (anchor.innerText == href)
              anchor.innerText = newHref
          }
        }
      case _ =>
    }
    for (e <- element.children)
      mapUrlsDom(e, f)
  }

  def mapUrls(string: String, f: URI => URI): String = {
    val dom = stringToDom(string)
    mapUrlsDom(dom, f)
    domToString(dom)
  }

  def domToString(element: Element): String = {
    element.outerHTML
  }
}
