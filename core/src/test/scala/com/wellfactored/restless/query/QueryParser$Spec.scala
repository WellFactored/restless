package com.wellfactored.restless.query

import atto.Atto._
import atto.ParseResult._
import com.wellfactored.restless.query.QueryAST._
import com.wellfactored.restless.query.QueryParser._
import org.scalatest.{FlatSpec, Matchers, WordSpecLike}

import scala.language.implicitConversions

class QueryParser$Spec extends WordSpecLike with Matchers {

  "identifier" should {
    "match a" in {
      identifier.parseOnly("a") shouldBe Done("", "a")
    }

    "match _a" in {
      identifier.parseOnly("_a") shouldBe Done("", "_a")
    }

    "match a3_b" in {
      identifier.parseOnly("a3_b") shouldBe Done("", "a3_b")
    }

    "not match 3a" in {
      identifier.parseOnly("2a") shouldBe a[Fail]
    }
  }

  "path" should {
    "match a" in {
      path.parseOnly("a") shouldBe Done("", Path("a"))
    }


    "match a.b" in {
      path.parseOnly("a.b") shouldBe Done("", Path("a.b"))
    }
  }
  "stringComparison" should {
    """match a = "foo"""" in {
      stringComparison.parseOnly("""a = "foo"""") shouldBe Done("", SEQ(Path("a"), "foo"))
    }

    """match a != "foo"""" in {
      stringComparison.parseOnly("""a != "foo"""") shouldBe Done("", SNEQ(Path("a"), "foo"))
    }

    """match a starts-with "foo"""" in {
      stringComparison.parseOnly("""a starts-with "foo"""") shouldBe Done("", StartsWith(Path("a"), "foo"))
    }

    """match a starts with "foo"""" in {
      stringComparison.parseOnly("""a starts with "foo"""") shouldBe Done("", StartsWith(Path("a"), "foo"))
    }

    """match a ends-with "foo"""" in {
      stringComparison.parseOnly("""a ends-with "foo"""") shouldBe Done("", EndsWith(Path("a"), "foo"))
    }

    """match a ends with "foo"""" in {
      stringComparison.parseOnly("""a ends with "foo"""") shouldBe Done("", EndsWith(Path("a"), "foo"))
    }

    """match a contains "foo"""" in {
      stringComparison.parseOnly("""a contains "foo"""") shouldBe Done("", Contains(Path("a"), "foo"))
    }
  }

  implicit def numconstd(d: Double): NumberRef = NumberConstant(d)

  implicit def numconsti(i: Int): NumberRef = NumberConstant(i)

  "numberComparison" should {
    "match a = 3.0" in {
      numberComparison.parseOnly("a = 3.0") shouldBe Done("", EQ(Path("a"), 3.0))
    }

    "match a < 3" in {
      numberComparison.parseOnly("a < 3") shouldBe Done("", LT(Path("a"), 3.0))
    }
    "match a >  3" in {
      numberComparison.parseOnly("a >  3") shouldBe Done("", GT(Path("a"), 3.0))
    }
    "match a != 3.0" in {
      numberComparison.parseOnly("a != 3.0") shouldBe Done("", NEQ(Path("a"), 3.0))
    }

    "match a<=3.0" in {
      numberComparison.parseOnly("a<=3.0") shouldBe Done("", LE(Path("a"), 3.0))
    }
    "match a>=-3" in {
      numberComparison.parseOnly("a>=-3") shouldBe Done("", GE(Path("a"), -3))
    }
  }
  "conjunction" should {
    "match a = 3 and b = 2" in {
      conjunction.parseOnly("a = 3 and b = 2") shouldBe Done("", AND(EQ(Path("a"), 3), EQ(Path("b"), 2)))
    }

    "match a = 3 or b = 2" in {
      conjunction.parseOnly("a = 3 or b = 2") shouldBe Done("", OR(EQ(Path("a"), 3), EQ(Path("b"), 2)))
    }
    "match a = 3 and b = 2 or c = 5" in {
      conjunction.parseOnly("a = 3 and b = 2 or c = 5") shouldBe Done("", AND(EQ(Path("a"), 3.0), OR(EQ(Path("b"), 2.0), EQ(Path("c"), 5.0))))
    }

    "match (a = 3 and b = 2) or c = 5" in {
      conjunction.parseOnly("(a = 3 and b = 2) or c = 5") shouldBe Done("", OR(AND(EQ(Path("a"), 3.0), EQ(Path("b"), 2.0)), EQ(Path("c"), 5.0)))
    }

    "match a = 3 and (b = 2 or c = 5)" in {
      conjunction.parseOnly("a = 3 and (b = 2 or c = 5)") shouldBe Done("", AND(EQ(Path("a"), 3.0), OR(EQ(Path("b"), 2.0), EQ(Path("c"), 5.0))))
    }

    "match (a = 3 and b = 2) or (c = 5 and d = 6)" in {
      conjunction.parseOnly("(a = 3 and b = 2) or (c = 5 and d = 6)") shouldBe Done("", OR(AND(EQ(Path("a"), 3.0), EQ(Path("b"), 2.0)), AND(EQ(Path("c"), 5.0), EQ(Path("d"), 6.0))))
    }

    "match ((a = 3 and b = 2) or (c = 5 and d = 6)) and x = 9" in {
      conjunction.parseOnly("((a = 3 and b = 2) or (c = 5 and d = 6)) and x = 9") shouldBe
        Done("",
          AND(
            OR(
              AND(EQ(Path("a"), 3.0), EQ(Path("b"), 2.0)),
              AND(EQ(Path("c"), 5.0), EQ(Path("d"), 6.0))),
            EQ(Path("x"), 9.0)))
    }
  }
  "query" should {
    "match a = 3" in {
      query.parseOnly("a = 3") shouldBe Done("", EQ(Path("a"), 3.0))
    }


    "match (a = 3)" in {
      query.parseOnly("(a = 3)") shouldBe Done("", EQ(Path("a"), 3.0))
    }
    "match a = 3 or b = 2" in {
      query.parseOnly("a = 3 or b = 2") shouldBe Done("", OR(EQ(Path("a"), 3.0), EQ(Path("b"), 2.0)))
    }
    "fail to match" in {
      // Checking that this does not put the parser into an infinite loop
      query.parseOnly("""a starts-ith "foo"""") shouldBe a[Fail]
    }

    "parse an 'and' expression" in {
      query.parseOnly("""a = 1 and b = "x"""") shouldBe a[Done[_]]
    }
  }
}
