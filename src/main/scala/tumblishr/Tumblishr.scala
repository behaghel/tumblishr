package org.behaghel.tumblishr

import io._
import dispatch._
import json._
import Js._
import Http._

class ReadBuilder(service: Request, params: Map[String, Any] = Map.empty)
    extends Builder[Handler[List[JsObject]]] {
    
  def param(key: String)(value: Any) = new ReadBuilder(service, params + (key -> value))

  /** the post offset to start from. Default 0 */
  def start(n: Int) = param("start")_
  /** the number of posts to return. Default 20, max 50. */
  def num(n: Int) = param("num")_
  /**
   * the type of posts to return.
   * @param t Symbol Possible values are: 'text, 'quote, 'photo, 'link,
   *                       'chat, 'video, or 'audio
   */
  def only(t: Symbol) = param("type")(t.name)
  /** a specific post id to return. */
  def id(id: String) = param("id")_
  /**
   * @return  post content as stored.
   *          If markdown was used to edit it, markdown is returned.
   */
  def raw = param("filter")("none")
  /** post returned as text, no HTML. */
  def text = param("filter")("text")
  /** returns posts with tag. */
  def tagged(t: String) = param("tagged")_
  /** returns posts that match query. */
  def search(q: String) = param("search")_

  /**
   * because tumblr json apis are not really json...
   * @param in java.io.InputStream the response from Tumblr.
   * @return  JsValue that was parsed.
   */
  private def tumblrJsonify(in: java.io.InputStream) = {
    val s = Source.fromInputStream(in)
    val ls = s.toList
    val jsonStr = ls.slice("var tumblr_api_read = ".length, ls.length - 2).mkString
    JsValue.fromString(jsonStr)
  }

  def product = (service / "api/read/json" <<? params >> { tumblrJsonify _ }) ~> (list ! obj)
  def fetch = product
}


class Tumblishr(url: String) {
  val service = :/(url)

  def read = new ReadBuilder(service)

  // some pattern matcher for the read API
  def posts = 'posts ? (list ! obj)


  // write API
  def postMarkdown(email: String, password: String, filePath: String, draft: Boolean = false) = {

    val (title, content, tags) = MarkdownPost.parse(filePath)

    lazy val params: Map[String, Any] = Map("email" -> email, "password" -> password,
      "type"      -> "regular",
      "generator" -> "Tumblishr v1.0",
      "private"   -> 0,
      "format"    -> "markdown",
      "slug"      -> slug,
      "state"     -> state,
      "title"     -> title,
      "body"      -> content,
      "tags"      -> tags.getOrElse(""))
    
    lazy val PathFinder = ".*/".r
    lazy val slug =  PathFinder.replaceAllIn(filePath, "")
    
    lazy val state = if (draft) "draft" else "published"

  }
}

class MalformedPostException(msg: String) extends Exception(msg)

object MarkdownPost {
  
  val TagsFinder = "tags\\s*[=:]\\s*((?:\\w+(?:,\\s?)?)+)".r
  val TitleFinder = "^#([^#].*)(?:#+)?$".r
  import TitleFinder.{pattern => TitlePattern}

  def parse(fp: String): (String, String, Option[String]) = {
    import io.Source
    val lines = Source.fromFile(fp).getLines

    // title
    val title = extractTitle(lines)

    // content
    val content = extractContent(lines)
    val tags = extractTags(lines)
    (title, content, tags)
  }

  def extractTitle(ls: Iterator[String]) = {
    val tl = ls find { TitlePattern.matcher(_).matches } getOrElse {
      throw new MalformedPostException("Missing title: no line with h1 level")
    }
    val tm = TitlePattern.matcher(tl)
    tm.matches
    tm.group(1)
  }

  def extractContent(ls: Iterator[String]) = {
    ls.takeWhile(!TagsFinder.pattern.matcher(_).find).mkString
  }

  def extractTags(ls: Iterator[String]) = {
    val line = if (ls.hasNext) Some(ls.next) else None
    line match {
      case Some(TagsFinder(tags)) => Some(tags)
      case _ => None
    }
  }

}

object Tumblr {
  val readPath = "api/read/json"
  val writePath = "api/write"

}


object CodeComp extends Tumblishr("http://blog.behaghel.org")
