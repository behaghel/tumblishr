package tumblishr

import dispatch._
import Tumblishr._

object TumblishrMain {

  def main(args: Array[String]) {

    val (command, userOpt) = getOpt(args)

    implicit val lp = LoginPassword(userOpt)

    command match {
      case lc: LocalCommand => lc.execute()
      case rc: RemoteCommand => rc.execute()
    }
  }

  case class LoginPassword(u: Option[String]) extends Session {

    val password =
      if (u.isDefined)
        askPass()
      else
        Config.pass().getOrElse(exitNoUserPass)

    val username = u.getOrElse(Config.user().getOrElse(exitNoUserPass))

    // XXX must be lzy otherwise will exit process at evaluation.
    // FIXME remove me and replace me with a simple throwing of an exception
    // this is a matter of style...
    lazy val exitNoUserPass = { error(NoUserPassword); "" }

    def askPass(): String = {
      print("Password: ")
      readLine()
    }

  }

  def getOpt(args: Array[String]): (Command, Option[String]) = {

    def _getOpt(as: List[String], user: Option[String] = None): (Command, Option[String]) =
      as match {
        case "-u" :: u :: ps => _getOpt(ps, Some(u))
        case "--force" :: f :: Nil => (Publish(f, draft = false), user)
        case "-l" :: "-r" :: ps => (makeListRemote(ps), user)
        case "-l" :: Nil => (ListLocal, user)
        case "-g" :: file :: Nil => (Get(file), user)
        case "-c" :: file :: Nil => (Commit(file), user)
        case "-d" :: Nil => (Diff, user)
        case "-h" :: Nil => (Usage, None)
        case f :: Nil => (Publish(f, draft = true), user)
        case _ => (Error(BadArguments), None) // something wrong with args
      }

    if (args.length == 0)
      (Error(InsufficientArgs), None)
    else
      _getOpt(args.toList)
  }

  def makeListRemote(as: List[String], kind: PostKind = AllKinds): Command =
    as match {
      case Nil => ListRemote(kind, AllStates)
      case "draft" :: Nil => ListRemote(kind, Draft)
      case "queue" :: Nil => ListRemote(kind, Queue)
      case "submission" :: Nil => ListRemote(kind, Submission)
      case "text" :: xs => makeListRemote(xs, Text)
      case "quote" :: xs => makeListRemote(xs, Quote)
      case "photo" :: xs => makeListRemote(xs, Photo)
      case "link" :: xs => makeListRemote(xs, Link)
      case "chat" :: xs => makeListRemote(xs, Chat)
      case "video" :: xs => makeListRemote(xs, Video)
      case "audio" :: xs => makeListRemote(xs, Audio)
      case _ => Error(BadArguments) // something wrong with args
    }

  sealed trait Command
  trait LocalCommand extends Command {
    def execute()
  }
  trait RemoteCommand extends Command {
    def execute()(implicit s: Session)
  }
  trait TodoCommand extends LocalCommand {
    def execute() {
      error(UnsupportedOperation)
    }
  }
  case object Usage extends LocalCommand {
    def execute() {
      println(usage)
    }
  }
  case object ListLocal extends TodoCommand // TODO LocalCommand
  case class Error(rc: ReturnCode) extends LocalCommand {
    def execute() {
      error(rc)
    }
  }

  case class Publish(filename: String, draft: Boolean) extends RemoteCommand {
    def execute()(implicit s: Session) {
      val http = new Http
      http(postMarkdown(filename, draft))
    }
  }
  case class ListRemote(kind: PostKind, state: PostState) extends TodoCommand // TODO RemoteCommand
  case object Diff extends TodoCommand // TODO RemoteCommand
  case class Commit(filename: String) extends TodoCommand // TODO RemoteCommand
  case class Get(filename: String) extends TodoCommand // TODO RemoteCommand

  def usage = {
    val text = "To publish a post:" ::
      "\ttum [-u <user>] [--force] <file>" ::
      "To list posts:" ::
      "\ttum [-u <user>] -l [-r [<type>[ <status>]]]" ::
      "To print diff between local and tumblr" ::
      "\ttum [-u <user>] -d" ::
      "To get remote post into local file:" ::
      "\ttum [-u <user>] -g <file>" ::
      "To commit a local change to a remote post:" ::
      "\ttum [-u <user>] -c <file>" ::
      "To print this help:" ::
      "\ttum -h" ::
      Nil
    text.mkString("\n")
  }

  sealed trait PostKind
  case object AllKinds extends PostKind
  case object Text extends PostKind
  case object Quote extends PostKind
  case object Photo extends PostKind
  case object Link extends PostKind
  case object Chat extends PostKind
  case object Video extends PostKind
  case object Audio extends PostKind

  sealed trait PostState
  case object AllStates extends PostState
  case object Draft extends PostState
  case object Queue extends PostState
  case object Submission extends PostState

  sealed abstract class ReturnCode(val rc: Int)
  case object WellDone extends ReturnCode(0)
  case object InsufficientArgs extends ReturnCode(64)
  case object BadArguments extends ReturnCode(64)
  case object NoUserPassword extends ReturnCode(67)
  case object UnsupportedOperation extends ReturnCode(69)

  def error(rc: ReturnCode) {
    print("error: ")
    println(rc match {
      case NoUserPassword => "Pair user/password undefined: use -u or ~/.tumblishrc."
      case InsufficientArgs => "missing args.\n" + usage
      case BadArguments => "bad args.\n" + usage
      case _ => "Something sad happened. We couldn't process your command."
    })
    System.exit(rc.rc)
  }

  object Config {
    import net.lag.configgy.Configgy
    val filename = System.getProperty("user.home") + "/.tumblishrc"
    // TODO what if file does not exists

    lazy val config = { Configgy.configure(filename); Configgy.config }

    def pass(): Option[String] = config.getString("pass")

    def user(): Option[String] = config.getString("user")

  }
}
