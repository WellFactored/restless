package com.wellfactored.restless.query

// our abstract syntax tree model
object QueryAST {

  sealed trait Query

  case object All extends Query

  case class Path(names: List[String])

  object Path {
    def apply(s: String): Path = new Path(s.split('.').toList)
  }

  sealed trait NumberRef

  case class NumberConstant(d: Double) extends NumberRef

  case class NumberPath(p: Path) extends NumberRef

  sealed trait Comparison extends Query

  sealed trait NumberComparison extends Comparison

  case class GT(lhs: Path, rhs: NumberRef) extends NumberComparison

  case class GE(lhs: Path, rhs: NumberRef) extends NumberComparison

  case class LT(lhs: Path, rhs: NumberRef) extends NumberComparison

  case class LE(lhs: Path, rhs: NumberRef) extends NumberComparison

  case class EQ(lhs: Path, rhs: NumberRef) extends NumberComparison

  case class NEQ(lhs: Path, rhs: NumberRef) extends NumberComparison

  sealed trait StringComparison extends Comparison

  case class SEQ(lhs: Path, rhs: String) extends StringComparison

  case class SNEQ(lhs: Path, rhs: String) extends StringComparison

  case class StartsWith(lhs: Path, rhs: String) extends StringComparison

  case class EndsWith(lhs: Path, rhs: String) extends StringComparison

  case class Contains(lhs: Path, rhs: String) extends StringComparison

  sealed trait Conjunction extends Query

  case class AND(lhs: Query, rhs: Query) extends Conjunction

  case class OR(lhs: Query, rhs: Query) extends Conjunction

  trait Conj {
    def make(left: Query, right: Query): Conjunction
  }

  object Conj {

    case object and extends Conj {
      override def make(left: Query, right: Query): Conjunction = AND(left, right)
    }

    case object or extends Conj {
      override def make(left: Query, right: Query): Conjunction = OR(left, right)
    }

  }

}
