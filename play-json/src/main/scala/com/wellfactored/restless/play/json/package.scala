package com.wellfactored.restless.play

import atto.ParseResult.Done
import com.wellfactored.restless.QueryAST.Query
import com.wellfactored.restless.QueryParser
import play.api.libs.json._

package object json {

  import atto.Atto._
  import com.wellfactored.restless.QueryPrinter._

  implicit val queryFormat = new Format[Query] {
    override def reads(json: JsValue): JsResult[Query] = {
      implicitly[Reads[String]].reads(json).flatMap { qs =>
        QueryParser.query.parseOnly(qs) match {
          case Done(_, q) => JsSuccess(q, JsPath())
          case _ => JsError("failed to parse query string")
        }
      }
    }

    override def writes(q: Query): JsValue = JsString(print(q))
  }
}
