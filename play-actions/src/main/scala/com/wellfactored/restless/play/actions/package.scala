package com.wellfactored.restless.play

import atto.Atto._
import atto.ParseResult.Done
import com.wellfactored.restless.query.QueryAST.{Path, Query}
import com.wellfactored.restless.query.{QueryAST, QueryParser, QueryPrinter}
import play.api.libs.json._
import play.api.mvc.QueryStringBindable

package object actions {
  implicit val pathR = new Reads[Path] {
    override def reads(json: JsValue): JsResult[Path] = implicitly[Reads[String]].reads(json).flatMap { js =>
      JsSuccess(Path(js), JsPath(List()))
    }
  }

  implicit val queryBinding = new QueryStringBindable[QueryAST.Query] {
    override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, Query]] = {
      params.get(key).map { qs =>
        QueryParser.query.parseOnly(qs.headOption.getOrElse("")) match {
          case Done(_, q) => Right(q)
          case _ => Left("failed to parse query string")
        }
      }
    }

    override def unbind(key: String, value: Query): String = QueryPrinter.print(value)
  }


  implicit val queryFormat = new Format[Query] {
    override def reads(json: JsValue): JsResult[Query] = {
      implicitly[Reads[String]].reads(json).flatMap { qs =>
        QueryParser.query.parseOnly(qs) match {
          case Done(_, q) => JsSuccess(q, JsPath())
          case _ => JsError("failed to parse query string")
        }
      }
    }

    override def writes(q: Query): JsValue = JsString(QueryPrinter.print(q))
  }
}
