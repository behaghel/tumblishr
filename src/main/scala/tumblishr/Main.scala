package tumblishr

import dispatch._

object TumblishrMain {
  
  def main(args: Array[String]) {
    //println("hello, world!")
    // println(null==args)
    // TODO: manage args
    val postFile = args(0)
    // TODO: use configgy to retrieve those values
    val user = "behaghel@gmail.com"
    val pass = "AnVSGhX=,eEb{0!oI\\So(W+E}"
    //println("Posting %s on behalf of %s (%s)".format(postFile, user, pass))
    val http = new Http
    http(Tumblishr.postMarkdown(user, pass, postFile, true))
    
  }
}
