package com.wellfactored.restless.play.json

import com.wellfactored.restless.query.QueryAST.Path
import org.scalatest.{Matchers, WordSpecLike}
import play.api.libs.json.Json

class JsSortFnSpec extends WordSpecLike with Matchers {

  val j1 = Json.parse("""{ "a" : 1 , "b" : 1 }""")
  val j2 = Json.parse("""{ "a" : 2 , "b" : 2 }""")
  val j3 = Json.parse("""{ "a" : 3 }""")

  import Selection.jsSortFn

  "jsSortFn" should {
    "sort numbers in ascending order" in {
      val f1 = jsSortFn(Path("a"), rev = false)
      f1(j1, j2) shouldBe true
      f1(j2, j1) shouldBe false
    }

    "sort missing field after those that have the field" in {
      val f = jsSortFn(Path("b"), rev = false)
      f(j1, j3) shouldBe true
      f(j3, j1) shouldBe false

      Seq(j3, j2, j1).sortWith(f) shouldBe Seq(j1, j2, j3)
    }

    "sort missing field after those that have the field even when reversed" in {
      val f = jsSortFn(Path("b"), rev = true)
      f(j1, j3) shouldBe true
      f(j3, j1) shouldBe false

      Seq(j3, j2, j1).sortWith(f) shouldBe Seq(j2, j1, j3)
    }
  }

}
