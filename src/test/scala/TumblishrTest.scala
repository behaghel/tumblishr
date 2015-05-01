package tumblishr

import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers
import io.Source

import Tumblishr._

// TODO separate internal Specs from public ones.
class TumblishrSpec extends FunSpec with ShouldMatchers {
  describe("MarkdownPost") {
    describe ("when file has a title, a content and tags") {
      it ("should detect this structure") {
        val post = """
                    |# this is my title
                    |this is my content.
                    |<!-- tags: some,cool,tags -->
                    |""".stripMargin
        val expectedTitle = "this is my title"
        val expectedContent = "this is my content.\n"
        val expectedTags = Some("some,cool,tags")
        val lines = Source.fromString(post).getLines
        assert((expectedTitle, expectedContent, expectedTags) === MarkdownPost.parseLines(lines))
      }
    }
    describe ("when file does not exist") {
      it ("should throw a FileNotFoundException") {
        evaluating {MarkdownPost.parseFile("/neitherhere/northere")} should produce [java.io.FileNotFoundException]
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
        val tags = MarkdownPost.extractContent(ls)._2
        tags should be (None)
      }
    }
    describe ("when tags contains non alpha char") {
      it ("should accept them") {
        val post = """
                    |# this is my title
                    |this is my content.
                    |<!-- tags: by:hub,code-gen,very_important!,@twitter,money$$$ -->
                    |""".stripMargin
        val expectedTags = Some("by:hub,code-gen,very_important!,@twitter,money$$$")
        val lines = Source.fromString(post).getLines
        assert(expectedTags === MarkdownPost.parseLines(lines)._3)

      }
    }
    describe ("when file is given as a path") {
      it ("should keep only its basename without extension as slug (human readable end of URL)") {
        val fp = "/some/where/over/the/rainbow.way"
        assert("rainbow" === MarkdownPost.slugFromFile(fp))
      }
    }
    describe ("when file is encoded with something else than utf-8") {
      it ("should be reencoded in UTF-8") {
        pending
      }
    }
  }
  describe ("ReadBuilder") {
    describe ("when parametrized once with security") {
      it ("should remain secure whatever other param we may add") {
        pending
      }
    }
  }
}

