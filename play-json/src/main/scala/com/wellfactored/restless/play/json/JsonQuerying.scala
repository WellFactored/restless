package com.wellfactored.restless.play.json

import play.api.libs.json._
import com.wellfactored.restless.query.QueryAST._

import scala.annotation.tailrec

/**
  * This is an execution engine to apply a Query AST to a document in the form of a JsObject.
  *
  * TODO: combine SEQ/SNEQ with EQ/NEQ
  */
object JsonQuerying {
  def query(q: Query)(implicit doc: JsValue): Boolean = {
    q match {
      case All => true
      case SEQ(path, s) => testString(path)(_.equalsIgnoreCase(s))
      case SNEQ(path, s) => testString(path)(v => !v.equalsIgnoreCase(s))
      case StartsWith(path, s) => testString(path)(_.toUpperCase.startsWith(s.toUpperCase))
      case EndsWith(path, s) => testString(path)(_.toUpperCase.endsWith(s.toUpperCase))
      case Contains(path, s) => testString(path)(_.toUpperCase.contains(s.toUpperCase))

      case EQ(path, ref) => testNumber(path, ref)(_ == _)
      case NEQ(path, ref) => testNumber(path, ref)(_ != _)
      case GT(path, ref) => testNumber(path, ref)(_ > _)
      case GE(path, ref) => testNumber(path, ref)(_ >= _)
      case LT(path, ref) => testNumber(path, ref)(_ < _)
      case LE(path, ref) => testNumber(path, ref)(_ < _)

      case AND(q1, q2) => query(q1) && query(q2)
      case OR(q1, q2) => query(q1) || query(q2)
    }
  }

  def testString(path: Path)(test: (String) => Boolean)(implicit doc: JsValue): Boolean = {
    doc match {
      case JsArray(as) => as.exists(testString(path)(test)(_))
      case _ => lookup(path) match {
        case JsArray(as) => as.collect {
          case a: JsString => a.value
        }.exists(test)
        case JsString(j) => test(j)
        case _ => false
      }
    }
  }

  def testNumber(path: Path, ref: NumberRef)(test: (Double, Double) => Boolean)(implicit doc: JsValue): Boolean = {
    ref match {
      case NumberConstant(d) => testNumber(path)(test(_, d))
      case NumberPath(p) => lookup(p) match {
        case JsNumber(j) => testNumber(path)(test(_, j.doubleValue()))
        case _ => false
      }
    }
  }

  def testNumber(path: Path)(test: (Double) => Boolean)(implicit doc: JsValue): Boolean = {
    doc match {
      case JsArray(as) => as.exists(testNumber(path)(test)(_))
      case _ => lookup(path) match {
        case JsArray(as) => as.collect {
          case a: JsNumber => a.value.doubleValue
        }.exists(test)
        case JsNumber(j) => test(j.doubleValue)
        case _ => false
      }
    }
  }

  def lookup(path: Path)(implicit jv: JsValue): JsValue = {
    path.names match {
      case Nil => jv
      case n :: rest => jv match {
        case _: JsObject => lookup(Path(rest))((jv \ n).getOrElse(JsNull))
        case as: JsArray => JsArray(as.value.map(a => lookup(Path(rest))((a \ n).getOrElse(JsNull))))
        case _ => JsNull
      }
    }
  }
}
