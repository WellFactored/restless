package com.wellfactored.restless.play.json

import com.wellfactored.restless.QueryAST.{Path, Query}
import play.api.libs.json._

import scala.language.implicitConversions

object Selection {

  implicit class SelectionOps[T: Writes](xs: Seq[T]) {
    def limit(l: Option[Int]): Seq[T] = l match {
      case Some(i) => xs.take(i)
      case None => xs
    }

    def where(qo: Option[Query]): Seq[T] = qo match {
      case None => xs
      case Some(q) => xs.filter { x =>
        Json.toJson(x) match {
          case doc: JsObject => JsonQuerying.query(q)(doc)
          case _ => false
        }
      }
    }
  }

  implicit def filterFn[T: Writes](qo: Option[Query]): T => Boolean = qo match {
    case None => _ => true
    case Some(q) => filterFn(q)
  }

  implicit def filterFn[T: Writes](q: Query): T => Boolean = { x =>
    Json.toJson(x) match {
      case doc: JsObject => JsonQuerying.query(q)(doc)
      case _ => false
    }
  }

  def selectJson[T: Writes, B](ts: Seq[T], qo: Option[Query], eo: Option[List[Path]], maxResults: Option[Int])(sortKey: (T) => B)(implicit ordering: Ordering[B]): Seq[JsValue] = {

    val projection: T => JsValue = eo.map { paths =>
      new JsonProjector[T](paths.map(_.names)).project(_)
    }.getOrElse(new JsonIdentity[T].project(_))

    def isEmpty(jv: JsValue): Boolean = jv match {
      case JsObject(e) if e.isEmpty => true
      case JsNull => true
      case _ => false
    }

    def nonEmpty(jv: JsValue): Boolean = !isEmpty(jv)

    ts.filter(qo)
      .sortBy(sortKey)
      .map(projection)
      .filter(nonEmpty)
      .limit(maxResults)
  }
}
