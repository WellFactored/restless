package com.wellfactored.restless.play

import atto.Atto._
import atto.ParseResult.Done
import com.wellfactored.restless.query.QueryAST.{Path, Query}
import com.wellfactored.restless.query.{QueryAST, QueryParser, QueryPrinter}
import play.api.libs.json.{Json, _}
import play.api.mvc.QueryStringBindable

package object actions {
  implicit val pathR = new Reads[Path] {
    override def reads(json: JsValue): JsResult[Path] = implicitly[Reads[String]].reads(json).flatMap { js =>
      JsSuccess(Path(js.split('.').toList), JsPath(List()))
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

  /**
    * I'm using snake-case names here as that is more conventional for the web side of things. This class
    * represents a structure that will be passed in via json.
    *
    * TODO: Change the names to scala-style camel-case and implement a Json.reads that binds the snake-case
    * names from the json
    */
  case class Params(
                     page_number: Option[Int],
                     page_size: Option[Int],
                     max_results: Option[Int],
                     query: Option[Query],
                     fields: Option[List[Path]])

  object Params {
    val empty = Params(None, None, None, None, None)
    implicit val paramsR = Json.reads[Params]
  }

}
