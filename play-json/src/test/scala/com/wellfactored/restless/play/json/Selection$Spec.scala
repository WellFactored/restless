package com.wellfactored.restless.play.json

import com.wellfactored.restless.query.QueryAST
import com.wellfactored.restless.query.QueryAST.Path
import org.scalatest.{FlatSpec, Matchers}
import play.api.libs.json.{JsObject, JsString, Json}

class Selection$Spec extends FlatSpec with Matchers {

  import Selection._

  case class Foo(id: Long, s: Option[String] = None, i: Option[Int] = None)

  implicit val fooW = Json.writes[Foo]


  "selectJson" should "sort by id" in {
    val foos = Seq(Foo(2), Foo(1))
    val expected = Seq(Foo(1), Foo(2)).map(Json.toJson(_))
    selectJson(foos, None, None, None)(_.id) shouldBe expected
  }

  it should "limit number of results" in {
    val foos = (1 to 50).map(Foo(_))
    val expected = foos.take(10).map(Json.toJson(_))

    selectJson(foos, None, None, Some(10))(_.id) shouldBe expected
  }

  it should "not limit number of results" in {
    val foos = (1 to 50).map(Foo(_))
    val expected = foos.map(Json.toJson(_))

    selectJson(foos, None, None, None)(_.id) shouldBe expected
  }

  it should "project the right fields" in {
    val foos = (1 to 50).map(n => Foo(n, Some(s"s$n"), Some(3)))
    val projection = List(Path("id"), Path("s"))
    val expected = (1 to 50).map(n => Foo(n, Some(s"s$n"), None)).map(Json.toJson(_))

    selectJson(foos, None, Some(projection), None)(_.id) shouldBe expected
  }

  it should "filter empty objects" in {
    val foos = (1 to 10).map(i => Foo(i, if (i % 2 == 0) Some(s"s$i") else None, None))
    val projection = List(Path("s"))
    val expected = (1 to 10).flatMap(i => if (i % 2 == 0) Some(JsObject(Seq("s" -> JsString(s"s$i")))) else None)

    val results = selectJson(foos, None, Some(projection), None)(_.id)
    results shouldBe expected
  }

  it should "de-duplicate results" in {
    val foos = (1 to 10).map(Foo(_, Some("s"), None))
    val projection = List(Path("s"))
    val expected = Vector(JsObject(Seq("s" -> JsString("s"))))

    val results = selectJson(foos, None, Some(projection), None)(_.id)
    results shouldBe expected
  }

}
