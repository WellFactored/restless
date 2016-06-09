package com.wellfactored.restless.play.actions

import atto.ParseResult.Done
import com.wellfactored.restless.query.QueryAST.Path
import com.wellfactored.restless.query.QueryParser
import play.api.libs.json.{JsArray, JsObject, Json, Writes}
import play.api.mvc.Results._
import play.api.mvc._

import scala.util.control.NonFatal
import scala.util.{Failure, Success, Try}

class CollectionRequest(val request: Request[String], val params: Params) extends WrappedRequest[String](request)

object ApiActions extends BodyParsers {

  def JsCollect(js: => Seq[JsObject]): Action[String] = Collection { implicit request =>
    import Selector._
    Ok(Json.toJson(js.jsSelect(request.params)))
  }

  def Collect[T: Writes, B](xs: => Iterable[T])(sortKey: (T) => B)(implicit ordering: Ordering[B]): Action[String] = Collection { implicit request =>
    import Selector._
    Ok(Json.toJson(xs.select(request.params)(sortKey)))
  }

  def Collection(f: CollectionRequest => Result): Action[String] = Action(parse.tolerantText) { implicit request =>
    withCollectionParams(params => f(new CollectionRequest(request, params)))
  }

  def withCollectionParams(f: Params => Result)(implicit request: Request[String]): Result =
    extractParams.fold(result => result, params => f(params))

  def extractParams(implicit request: Request[String]): Either[Result, Params] = {
    request.method match {
      case "POST" => extractFromJson
      case "GET" => extractFromQueryParams(request.queryString)
      case m => Left(BadRequest(s"Invalid html method type: $m"))
    }
  }

  /**
    * TODO: handle errors in the query parser
    * TODO: handle errors in the fields parser
    */
  def extractFromQueryParams(params: Map[String, Seq[String]]): Either[Result, Params] = {

    import atto._
    import Atto._

    val pageNumber = params.get("page_number").flatMap(_.headOption.map(_.toInt))
    val pageSize = params.get("page_size").flatMap(_.headOption.map(_.toInt))
    val maxResults = params.get("max_results").flatMap(_.headOption.map(_.toInt))
    val query = params.get("query").flatMap {
      _.headOption.map { qs =>
        QueryParser.query.parseOnly(qs) match {
          case Done(_, q) => Right(q)
          case _ => Left("failed to parse query string")
        }
      }
    }

    /*
    * "fields" is expected to be a json array of strings. Each string is a dotted path into the
    * results structure.
     */
    val fields = params.get("fields").flatMap {
      _.headOption.flatMap { s =>
        Try(Json.parse(s)).toOption.flatMap { jv =>
          jv match {
            case JsArray(vs) => Some(vs.toList.flatMap(_.validate[String].asOpt.map(Path(_))))
            case _ => None
          }
        }
      }
    }

    val sortKey = params.get("sort_by").flatMap(_.headOption.map(Path(_)))
    val reverse: Option[Boolean] = params.get("reverse").flatMap {
      _.headOption.map {
        case "true" => true
        case _ => false
      }
    }

    Right(Params(pageNumber, pageSize, maxResults, query.map(_.right.get), fields, sortKey, reverse))
  }

  def extractFromJson(implicit request: Request[String]): Either[Result, Params] = {
    Try {
      // Annoying that there is no parse that doesn't throw an exception
      Json.parse(request.body).validate[Params].fold(
        errs => Left(BadRequest(errs.toString())),
        params => Right(params)
      )
    }.recover {
      case NonFatal(e) => Left(BadRequest(e.getMessage))
    } match {
      case Success(v) => v
      // We've already recovered non-fatal exceptions above, re-throw fatal ones here
      case Failure(t) => throw t
    }
  }
}
