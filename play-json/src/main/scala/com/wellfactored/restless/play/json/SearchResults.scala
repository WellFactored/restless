package com.wellfactored.restless.play.json

import play.api.libs.json._

case class SearchResults[T: Writes](items: Seq[T], totalResults: Int, pageNumber: Int, itemsPerPage: Int)

object SearchResults {
  implicit def formats[T: Writes] = new Writes[SearchResults[T]] {
    override def writes(o: SearchResults[T]): JsValue = {
      //implicit val w1 = play.api.libs.json.Writes.seq
      //implicit val tw = implicitly[Writes[T]]
      val resultsJson = Json.toJson(o.items)
      JsObject(Map(
        "results" -> resultsJson,
        "total_results" -> JsNumber(o.totalResults),
        "page_number" -> JsNumber(o.pageNumber),
        "items_per_page" -> JsNumber(o.itemsPerPage)
      ))
    }
  }
}
