package com.wellfactored.restless.play.json

import atto.Atto._
import com.wellfactored.restless.query.QueryAST.Path
import com.wellfactored.restless.query.QueryParser
import org.scalatest.{Matchers, WordSpecLike}
import play.api.libs.json._

class JsonQueryingSpec extends WordSpecLike with Matchers {
  val docText =
    """
      |{
      |  "x" : "xvalue",
      |  "z" : 1,
      |  "ys" : [
      |    { "y" : "y1" },
      |    { "y" : 2 }
      |  ]
      |}
    """.stripMargin

  val doc = Json.parse(docText).as[JsObject]

  "query" should {
    "match a string field in an object" in {
      val q = QueryParser.query.parseOnly("""x = "xvalue"""")
      JsonQuerying.query(q.option.get)(doc) shouldBe true
    }

    "match fields in an array" in {
      val q = QueryParser.query.parseOnly("""ys.y = "y1"""")
      JsonQuerying.query(q.option.get)(doc) shouldBe true
    }
  }

  "testString" should {
    "match values in an array" in {
      val a = (doc \ "ys").as[JsArray]
      JsonQuerying.testString(Path("y"))(_ == "y1")(a) shouldBe true
    }
  }

  "testNumber" should {
    "match values in an array" in {
      val a = (doc \ "ys").as[JsArray]
      JsonQuerying.testNumber(Path("y"))(_ == 2)(a) shouldBe true
    }
  }

  "lookup" should {
    "find a string" in {
      JsonQuerying.lookup(Path("x"))(doc) shouldBe JsString("xvalue")
    }
    "find a number" in {
      JsonQuerying.lookup(Path("z"))(doc) shouldBe JsNumber(1)
    }
    "find an array" in {
      JsonQuerying.lookup(Path("ys.y"))(doc) shouldBe JsArray(Seq(JsString("y1"), JsNumber(2)))
    }
  }

}
