package com.wellfactored.restless.play.json

import com.wellfactored.restless.query.QueryAST.{Path, Query}
import play.api.libs.json._

import scala.language.implicitConversions

object Selection {

  implicit class SelectionOps[T: Writes](xs: Iterable[T]) {
    def limit(l: Option[Int]): Iterable[T] = l match {
      case Some(i) => xs.take(i)
      case None => xs
    }

    def where(qo: Option[Query]): Iterable[T] = qo match {
      case None => xs
      case Some(q) => xs.filter { x =>
        Json.toJson(x) match {
          case doc: JsObject => JsonQuerying.query(q)(doc)
          case _ => false
        }
      }
    }

    def reverse(ro: Option[Boolean]): Iterable[T] = ro match {
      case Some(true) => xs.toSeq.reverse
      case _ => xs
    }
  }

  implicit def filterFn[T: Writes](qo: Option[Query]): T => Boolean = qo match {
    case None => _ => true
    case Some(q) => filterFn(q)
  }

  def filterFn[T: Writes](q: Query): T => Boolean = { x =>
    Json.toJson(x) match {
      case doc: JsObject => JsonQuerying.query(q)(doc)
      case _ => false
    }
  }

  def isEmpty(jv: JsValue): Boolean = jv match {
    case JsObject(e) if e.isEmpty => true
    case JsNull => true
    case _ => false
  }

  def nonEmpty(jv: JsValue): Boolean = !isEmpty(jv)

  def projection[T: Writes](po: Option[List[Path]], t: T): JsValue = po.map {
    paths => new JsonProjector[T](paths).project(t)
  }.getOrElse(new JsonIdentity[T].project(t))


  implicit def jsSortFn(po: Option[Path]): (JsValue, JsValue) => Boolean = po match {
    case None => (a, b) => false
    case Some(p) => jsSortFn(p)
  }

  def jsSortFn(p: Path): (JsValue, JsValue) => Boolean = { (o1, o2) =>
    import play.api.libs.json._
    import JsonProjector.project

    (project(o1, p), project(o2, p)) match {
      case (n1: JsNumber, n2: JsNumber) => n1.value < n2.value
      case (s1: JsString, s2: JsString) => s1.value < s2.value
      case (b1: JsBoolean, b2: JsBoolean) => b1.value < b2.value
      case (JsNull, JsNull) => false // neither object has the key so keep original order
      case (_, JsNull) => true // sort objects without the key after those with the keys
      case _ => false //  JsObjects, JsArrays and mixed types keep original order
    }
  }

  def selectT[T: Writes, B](ts: Iterable[T], qo: Option[Query], po: Option[List[Path]], maxResults: Option[Int], rev: Option[Boolean])(sortKey: (T) => B)(implicit ordering: Ordering[B]): Iterable[JsValue] = {
    ts.where(qo).toSeq
      .sortBy(sortKey)
      .map(projection(po, _))
      .filter(nonEmpty)
      .distinct
      .limit(maxResults)
      .reverse(rev)
  }

  def selectFromJson(js: Seq[JsValue], qo: Option[Query], po: Option[List[Path]], maxResults: Option[Int], sortKey: Option[Path], rev: Option[Boolean]): Iterable[JsValue] = {
    js.where(qo).toSeq
      .sortWith(sortKey)
      .map(projection(po, _))
      .filter(nonEmpty)
      .distinct
      .limit(maxResults)
      .reverse(rev)
  }
}
