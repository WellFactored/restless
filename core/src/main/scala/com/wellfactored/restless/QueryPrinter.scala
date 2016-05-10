package com.wellfactored.restless

import com.wellfactored.restless.QueryAST._

/**
  * Provide functions to convert a Query to it's String representation
  */
object QueryPrinter {
  def print(q: Query): String = {
    q match {
      case SEQ(path, s) => s"""${print(path)} = "$s""""
      case SNEQ(path, s) => s"""${print(path)} != "$s""""
      case StartsWith(path, s) => s"""${print(path)} starts with "$s""""
      case EndsWith(path, s) => s"""${print(path)} ends with "$s""""
      case Contains(path, s) => s"""${print(path)} contains "$s""""

      case EQ(path, ref) => s"${print(path)} = ${print(ref)}"
      case NEQ(path, ref) => s"${print(path)} != ${print(ref)}"
      case GT(path, ref) => s"${print(path)} > ${print(ref)}"
      case GE(path, ref) => s"${print(path)} >= ${print(ref)}"
      case LT(path, ref) => s"${print(path)} < ${print(ref)}"
      case LE(path, ref) => s"${print(path)} <= ${print(ref)}"

      case AND(q1, q2) => s"(${print(q1)}) and (${print(q2)})"
      case OR(q1, q2) => s"(${print(q1)}) or (${print(q2)})"
    }
  }

  def print(path: Path): String = path.names.mkString(".")

  def print(ref: NumberRef): String = ref match {
    case NumberConstant(d) => d.toString
    case NumberPath(path) => print(path)
  }
}
