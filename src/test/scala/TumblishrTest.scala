package org.behaghel.tumblishr

import org.scalatest.Spec
import org.scalatest.matchers.ShouldMatchers
import scala.collection.mutable.Stack
import io.Source

class TumblishrSpec extends Spec with ShouldMatchers {
  val post = """
        | # this is my title
        | this is my content.
        | tags: some,cool,tags
        |"""
  describe ("Post Writer") {
    describe("MarkdownPost") {
      describe ("when file does not exist") {
        it ("should throw a FileNotFoundException") {
          evaluating {MarkdownPost.parse("/neitherhere/northere")} should produce [java.io.FileNotFoundException]
        }
      }
      describe ("when post has no title") {
        it ("should throw a MalformedPostException") {
          val post = """"""
          val ls = Source.fromString(post).getLines
          evaluating {MarkdownPost.extractTitle(ls)} should produce [MalformedPostException]
        }
      }
      describe ("when post has no tags") {
        it ("should say None for tags") {
          val post = """
                      | # this is my title
                      | this is my content.
                      |""".stripMargin
          val ls = Source.fromString(post).getLines
          val tags = MarkdownPost.extractTags(ls)
          tags should be (None)
        }
      }
    }
  }
}

