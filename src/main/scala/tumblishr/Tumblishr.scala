package tumblishr

import io._
import dispatch._
import json._
import Js._
import Http.builder2product

object Tumblishr {
  type Username = String
  type Password = String

  trait Session {
    def username: Username
    def password: Password
  }

  class ReadBuilder(val service: Request, val params: Map[String, Any] = Map.empty)
    extends Builder[Handler[List[JsObject]]] {

    def param(key: String)(value: Any) =
      new ReadBuilder(service, params + (key -> value))
    def sparam(key: String)(value: Any) =
      new ReadBuilder(service, params + (key -> value)) with SecureReading

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
    /** must be authenticated
     * @param s Symbole Possible values are: 'submission, 'draft or 'queue
     */
    def inState(s: Symbol) = sparam("state")(s.name)

    /**
     * because tumblr json apis are not really json...
     * @param in java.io.InputStream the response from Tumblr.
     * @return  JsValue that was parsed.
     */
    def tumblrJsonify(in: java.io.InputStream) = {
      val s = Source.fromInputStream(in)
      val ls = s.toList
      val jsonStr = ls.slice("var tumblr_api_read = ".length, ls.length - 2).mkString
      JsValue.fromString(jsonStr)
    }

    def product =
      (service / "api/read/json" <<? params >> { tumblrJsonify _ }) ~> (list ! obj)
  }

  trait SecureReading { self: ReadBuilder =>
    override def param(key: String)(value: Any) =
      new ReadBuilder(service, params + (key -> value)) with SecureReading
    override def product =
      (service.secure / "api/read/json" << params >> { tumblrJsonify _ }) ~> (list ! obj)
  }

  class MyTumblr(val username: String) {

    val myService = :/(username + ".tumblr.com")

    // read API
    def read = new ReadBuilder(myService)

    // some pattern matcher for the read API
    def posts = 'posts ? (list ! obj)

  }

  object Tumblr {
    val com = :/("www.tumblr.com")
  }

  // TODO read and write should be separated as they connect to different service.
  // moreover, post should follow the same chained logic to be more consistent
  // ie post.as(user,pwd).markdownFile(myFile).inState(draft)

  // write API
  def postMarkdown(filePath: String, draft: Boolean = false)(implicit s: Session) = {

    val (title, content, tags) = MarkdownPost.parseFile(filePath)

    lazy val params: Map[String, Any] = Map("email" -> s.username,
      "password" -> s.password,
      "type" -> "regular",
      "generator" -> "Tumblishr v1.0",
      "private" -> 0,
      "format" -> "markdown",
      "slug" -> MarkdownPost.slugFromFile(filePath),
      "state" -> state,
      "title" -> title,
      "body" -> content,
      "tags" -> tags.getOrElse(""))

    lazy val state = if (draft) "draft" else "published"

    // TODO instead of redirecting on System.out, catch the return post ID and return it
    Tumblr.com.secure / "api/write" << params as_str

  }

  class MalformedPostException(msg: String) extends Exception(msg)

  /**
   * Utilities to manipulate markdown post following tumblishr conventions.
   * that is:<ul>
   * <li>the text is in utf-8.</li>
   * <li>the title of the post is the first h1 header.</li>
   * <li>then there is the body of the post in regular markdown format.</li>
   * <li>then, there is the tagline (or EOF if no tag).
   * Tags are listed in csv prepended with 'tags:'.
   * The tagline is a line most likely inside an HTML comment.</li>
   * </ul>
   * Filename is also used as a 'slug' to make the end of the URL human-friendly.
   */
  object MarkdownPost {

    val TagsFinder = "tags\\s*[=:]\\s*((?:[-a-zA-Z0-9:@!_()&$]+(?:,\\s?)?)+)".r
    val TitleFinder = "^#(?:\\s+)?([^#].*)(?:#+)?$".r
    import TitleFinder.{ pattern => TitlePattern }

    /**
     * File must be UTF-8 encoded!
     */
    def parseFile(fp: String): (String, String, Option[String]) = {
      // if I don't specify utf-8, it is not choosen implicitly (?!)
      val lines = Source.fromFile(fp, "utf-8").getLines
      parseLines(lines)
    }

    def parseLines(lines: Iterator[String]) = {
      // title
      val title = extractTitle(lines)
      // content
      val content = extractContent(lines)
      val body = content._1
      val tags = content._2
      (title, body, tags)
    }

    def extractTitle(ls: Iterator[String]) = {
      val tl = ls find { TitlePattern.matcher(_).matches } getOrElse {
        val msg = "Missing title: no line with h1 level " +
          "(ie starting with 1 # at the very beginning of the line)."
        throw new MalformedPostException(msg)
      }
      val tm = TitlePattern.matcher(tl)
      tm.matches
      tm.group(1)
    }

    // Scala regex are extractors but only for the whole region (use matches() and we want find())
    object TagsLine {
      def unapply(s: String): Option[String] = {
        val m = TagsFinder.pattern.matcher(s)
        if (m.find) Some(m.group(1)) else None
      }
    }

    /**
     * to parse beyond the title until a line containing tags (tags: tag1,tag2) 
     * or until EOF if no tags specified.
     * @param ls iterator of lines of post position right after the title line.
     * @param sb accumulator for recursion. Normal use case should let the default value.
     * @return a (String, Option[String]) with the post body first and 
     *         an optional csv String for tags.
     */
    def extractContent(
      ls: Iterator[String],
      sb: StringBuilder = new StringBuilder): (String, Option[String]) = {
      if (ls.isEmpty) (sb.toString, None)
      else ls.next match {
        case TagsLine(tags) => (sb.toString, Some(tags))
        case c => extractContent(ls, sb.append(c).append('\n'))
      }
    }

    lazy val PathFinder = ".*/".r
    lazy val ExtensionFinder = "\\.[A-Za-z0-9]+$".r
    def slugFromFile(filePath: String) = {
      ExtensionFinder.replaceFirstIn(PathFinder.replaceAllIn(filePath, ""), "")
    }
  }
}
