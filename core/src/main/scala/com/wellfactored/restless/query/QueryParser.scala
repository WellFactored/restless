package com.wellfactored.restless.query

import atto.Atto._
import atto._
import com.wellfactored.restless.query.QueryAST._

object QueryParser extends Whitespace {

  lazy val query: Parser[Query] = {
    conjunction.t | comparison.t <~ endOfInput
  }.named("query")

  lazy val conjunction: Parser[Conjunction] = {
    (comparison.t ~ conj.t ~ conjunction).map { case ((l, c), r) => c.make(l, r) } |
      (comparison.t ~ conj.t ~ comparison).map { case ((l, c), r) => c.make(l, r) } |
      (parens(conjunction).t ~ conj.t ~ comparison).map { case ((l, c), r) => c.make(l, r) } |
      (comparison.t ~ conj.t ~ parens(conjunction)).map { case ((l, c), r) => c.make(l, r) } |
      (parens(conjunction).t ~ conj.t ~ parens(conjunction)).map { case ((l, c), r) => c.make(l, r) }
  }.named("conjunction")

  lazy val conj: Parser[Conj] = and | or

  lazy val and = string("and") >| Conj.and
  lazy val or = string("or") >| Conj.or

  lazy val identifier: Parser[String] = {
    val startingChar: Parser[Char] = elem(c => c.isLetter || c == '_')
    val identifierChar: Parser[Char] = elem(c => c.isLetterOrDigit || c == '_')

    (startingChar ~ many(identifierChar)).map { case (c, cs) => c + cs.mkString }
  }.named("identifier")

  lazy val path: Parser[Path] = {
    (identifier ~ many(char('.') ~> identifier)).map { case (s, rest) => Path(List(s) ++ rest) }
  }.named("path")

  lazy val comparison: Parser[Comparison] = {
    numberComparison | stringComparison
  }

  lazy val stringComparison: Parser[StringComparison] = {
    pairByT(path, char('='), stringLiteral).map(SEQ.tupled) |
      pairByT(path, string("!="), stringLiteral).map(SNEQ.tupled) |
      pairByT(path, string("starts-with"), stringLiteral).map(StartsWith.tupled) |
      pairByT(path, string("starts with"), stringLiteral).map(StartsWith.tupled) |
      pairByT(path, string("ends-with"), stringLiteral).map(EndsWith.tupled) |
      pairByT(path, string("ends with"), stringLiteral).map(EndsWith.tupled) |
      pairByT(path, string("contains"), stringLiteral).map(Contains.tupled)
  }.named("string comparison")

  lazy val numberComparison: Parser[NumberComparison] = {
    pairByT(path, char('='), numberRef) -| EQ.tupled |
      pairByT(path, string("!="), numberRef) -| NEQ.tupled |
      pairByT(path, char('<'), numberRef).map(LT.tupled) |
      pairByT(path, char('>'), numberRef).map(GT.tupled) |
      pairByT(path, string("<="), numberRef).map(LE.tupled) |
      pairByT(path, string(">="), numberRef).map(GE.tupled)
  }.named("number comparison")

  lazy val numberRef: Parser[NumberRef] = {
    double -| NumberConstant |
      path -| NumberPath
  }


}

// Some extra combinators and syntax for coping with whitespace. Something like this might be
// useful in core but it needs some thought - borrowed from the atto source
trait Whitespace {

  // Syntax for turning a parser into one that consumes trailing whitespace
  implicit class TokenOps[A](self: Parser[A]) {
    def t: Parser[A] =
      self <~ takeWhile(c => c.isSpaceChar || c == '\n')
  }

  // Delimited list
  def sepByT[A](a: Parser[A], b: Parser[_]): Parser[List[A]] =
    sepBy(a.t, b.t)

  // Delimited pair, internal whitespace allowed
  def pairByT[A, B](a: Parser[A], delim: Parser[_], b: Parser[B]): Parser[(A, B)] =
    pairBy(a.t, delim.t, b)

}