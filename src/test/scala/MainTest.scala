package tumblishr

import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers
import TumblishrMain._

class MainTest extends FunSpec with ShouldMatchers {
    val user = "usertest"
    val userOpt = "-u " + user + " "
    val someUser = Some(user)

    val postFile = "mypost.mkd"

    describe ("Tumblishr launcher options parsing:") {
      describe("publishing") {
        it ("should publish draft with only '<file>'") {
          val args = Array[String](postFile)
          assert(getOpt(args) === ((Publish(postFile, draft=true), None)))
        }
        it ("should publish draft with  '-user <user> <file>'") {
          val s = userOpt+postFile
          val args = s.split(" ").toArray
          assert(getOpt(args) === ((Publish(postFile, draft=true), someUser)))
        }
        it ("should publish with '--force <file>'") {
          val s = "--force " + postFile 
          val args = s.split(" ").toArray
          assert(getOpt(args) === ((Publish(postFile, draft=false), None)))
        }
        it ("should publish with '-u <user> --force <file>'") {
          val s = userOpt + "--force " + postFile
          val args = s.split(" ").toArray
          assert(getOpt(args) === ((Publish(postFile, draft=false), someUser)))
        }
      }
      describe("listing") {
        it ("should list local posts with only '-l'") {
          val s = "-l"
          val args = s.split(" ").toArray
          assert(getOpt(args) === ((ListLocal, None)))
        }
        it ("should list all remote posts with '-l -r'") {
          val s = "-l -r"
          val args = s.split(" ").toArray
          assert(getOpt(args) === ((ListRemote(AllKinds, AllStates), None)))
        }
        it ("should list all remote posts of type text in draft state with '-l -r text draft'") {
          val s = "-l -r text draft"
          val args = s.split(" ").toArray
          assert(getOpt(args) === ((ListRemote(Text, Draft), None)))
        }
      }
      describe("diffing") {
        it ("should print a diff all remote posts with local edition with only '-d'") {
          val s = "-d"
          val args = s.split(" ").toArray
          assert(getOpt(args) === ((Diff, None)))
        }
        it ("should print a diff all remote posts with local edition with '-u <user> -d'") {
          val s = userOpt + "-d"
          val args = s.split(" ").toArray
          assert(getOpt(args) === ((Diff, someUser)))
        }
      }
      describe("getting remote state into local file") {
        it ("should retrieve remote post into local edition with '-u <user> -g <file>'") {
          val s = userOpt + "-g " + postFile
          val args = s.split(" ").toArray
          assert(getOpt(args) === ((Get(postFile), someUser)))
        }
      }
      describe("committing") {
        it ("should commit local modif into remote post with '-u <user> -c <file>'") {
          val s = userOpt + "-c " + postFile
          val args = s.split(" ").toArray
          assert(getOpt(args) === ((Commit(postFile), someUser)))
        }
      }
      describe("helping") {
        it ("should print usage with only '-h'") {
          val s = "-h"
          val args = s.split(" ").toArray
          assert(getOpt(args) === ((Usage, None)))
        }
      }
      describe("bad args") {
        it ("should print error with bad args") {
          val s = "-q winkwink"
          val args = s.split(" ").toArray
          assert(getOpt(args) === ((Error(BadArguments), None)))
        }
      }

      it ("should throw IAE when no user is given (-u or ~/.tumblishrc)") {
        pending
      }
    }
  
}
