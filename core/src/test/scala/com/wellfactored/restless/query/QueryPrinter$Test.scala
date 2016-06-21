package com.wellfactored.restless.query

import atto.Atto._
import com.wellfactored.restless.query.QueryParser._
import org.scalatest.{FlatSpec, Matchers, WordSpecLike}
import QueryPrinter.print
import com.wellfactored.restless.query.QueryAST.All

class QueryPrinter$Test extends WordSpecLike with Matchers {

  "query printer" should {
    "print All as 'all'" in {
      print(All) shouldBe "all"
    }

    "normalise a number to 1 dp" in {
      val q = "a = 1"
      val expected = "a = 1.0"
      query.parseOnly(q).done.option.map(print) shouldBe Some(expected)
    }

    "print a path equality" in {
      val q = "a = b"
      query.parseOnly(q).done.option.map(print) shouldBe Some(q)
    }
    "print a path inequality" in {
      val q = "a != b"
      query.parseOnly(q).done.option.map(print) shouldBe Some(q)
    }
    "print a path gt" in {
      val q = "a > b"
      query.parseOnly(q).done.option.map(print) shouldBe Some(q)
    }
    "print a path lt" in {
      val q = "a < b"
      query.parseOnly(q).done.option.map(print) shouldBe Some(q)
    }
    "print a path ge" in {
      val q = "a >= b"
      query.parseOnly(q).done.option.map(print) shouldBe Some(q)
    }
    "print a path le" in {
      val q = "a <= b"
      query.parseOnly(q).done.option.map(print) shouldBe Some(q)
    }

    "print a string equality" in {
      val q = """a = "x""""
      query.parseOnly(q).done.option.map(print) shouldBe Some(q)
    }
    "print a string inequality" in {
      val q = """a != "x""""
      query.parseOnly(q).done.option.map(print) shouldBe Some(q)
    }
    "print a string 'starts with'" in {
      val q = """a starts with "x""""
      query.parseOnly(q).done.option.map(print) shouldBe Some(q)
    }
    "print a string 'ends with'" in {
      val q = """a ends with "x""""
      query.parseOnly(q).done.option.map(print) shouldBe Some(q)
    }
    "print a string 'contains'" in {
      val q = """a contains "x""""
      query.parseOnly(q).done.option.map(print) shouldBe Some(q)
    }

    "normalise an 'and'" in {
      val q = """a = 1 and b = "x""""
      val expected = """(a = 1.0) and (b = "x")"""
      query.parseOnly(q).done.option.map(print) shouldBe Some(expected)
    }
    "normalise an 'or'" in {
      val q = """a = 1 or b = "x""""
      val expected = """(a = 1.0) or (b = "x")"""
      query.parseOnly(q).done.option.map(print) shouldBe Some(expected)
    }
  }
}

