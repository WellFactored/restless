package com.wellfactored.restless.play.actions

import com.wellfactored.restless.query.QueryAST.{Path, Query}
import play.api.libs.json.Json

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
                   fields: Option[List[Path]],
                   sort_by: Option[Path],
                   reverse:Option[Boolean])

object Params {
  val empty = Params(None, None, None, None, None, None, None)
  implicit val paramsR = Json.reads[Params]
}
