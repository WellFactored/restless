package com.wellfactored.restless

import atto.ParseResult.{Done, Fail, Partial}

import scala.annotation.tailrec
import scala.io.StdIn
import atto._
import Atto._

object QueryParserApp extends App {
  repl()

  @tailrec
  def repl(): Unit = {
    // TODO: Replace next three lines with `scala.Predef.readLine(text: String, args: Any*)`
    // once BUG https://issues.scala-lang.org/browse/SI-8167 is fixed
    print("---\nEnter expression > ")
    Console.out.flush()
    StdIn.readLine() match {
      case "" =>
      case line =>
        QueryParser.query.parseOnly(line) match {
          case Fail(_, _, err) => println(err)
          case Partial(_) =>
          case Done(_, p) => println("Result: " + p)
        }
        repl()
    }
  }
}
