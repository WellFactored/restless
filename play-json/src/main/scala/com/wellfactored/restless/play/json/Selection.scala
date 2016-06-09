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

  implicit def jsSortFn(po: Option[Path]): (JsObject, JsObject) => Boolean = po match {
    case None => (a, b) => false
    case Some(p) => jsSortFn(p)
  }

  def project(o: JsObject, path: Path): JsValue = {
    import play.api.libs.json._

    path.names.foldLeft(o: JsValue) {
      case (j, p) => j \ p match {
        case JsDefined(JsObject(e)) if e.isEmpty => JsNull
        case JsDefined(jv) => jv
        case JsUndefined() => JsNull
      }
    }
  }

  def jsSortFn(p: Path): (JsObject, JsObject) => Boolean = { (o1, o2) =>
    import play.api.libs.json._

    (project(o1, p), project(o2, p)) match {
      case (n1: JsNumber, n2: JsNumber) => n1.value < n2.value
      case (s1: JsString, s2: JsString) => s1.value < s2.value
      case (b1: JsBoolean, b2: JsBoolean) => b1.value < b2.value
      case (JsNull, JsNull) => false // neither object has the key so keep original order
      case (_, JsNull) => true // sort objects without the key after those with the keys
      case _ => false //  JsObjects, JsArrays and mixed types keep original order
    }
  }

  def selectJson[T: Writes, B](ts: Iterable[T], qo: Option[Query], eo: Option[List[Path]], maxResults: Option[Int])(sortKey: (T) => B)(implicit ordering: Ordering[B]): Iterable[JsValue] = {

    val projection: T => JsValue = eo.map {
      paths =>
        new JsonProjector[T](paths.map(_.names)).project(_)
    }.getOrElse(new JsonIdentity[T].project(_))

    def isEmpty(jv: JsValue): Boolean = jv match {
      case JsObject(e) if e.isEmpty => true
      case JsNull => true
      case _ => false
    }

    def nonEmpty(jv: JsValue): Boolean = !isEmpty(jv)

    ts.filter(qo).toSeq
      .sortBy(sortKey)
      .map(projection)
      .filter(nonEmpty)
      .distinct
      .limit(maxResults)
  }

  def selectFromJson(js: Seq[JsObject], qo: Option[Query], eo: Option[List[Path]], maxResults: Option[Int], sortKey: Option[Path]): Seq[JsValue] = {

    val projection: JsObject => JsValue = eo.map {
      paths =>
        new JsonProjector[JsObject](paths.map(_.names)).project(_)
    }.getOrElse(new JsonIdentity[JsObject].project(_))

    def isEmpty(jv: JsValue): Boolean = jv match {
      case JsObject(e) if e.isEmpty => true
      case JsNull => true
      case _ => false
    }

    def nonEmpty(jv: JsValue): Boolean = !isEmpty(jv)

    js.filter(qo)
      .sortWith(sortKey)
      .map(projection)
      .filter(nonEmpty)
      .distinct
      .limit(maxResults)
      .toSeq
  }
}
