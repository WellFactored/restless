package com.wellfactored.restless.play.actions

import com.wellfactored.restless.pagination.{ItemCount, PageNumber, ResultsPage}
import com.wellfactored.restless.play.json.SearchResults
import play.api.libs.json._

object Selector {

  import com.wellfactored.restless.play.json.Selection._

  /**
    * Pimp out any `Iterable[T : Writes]` with a `.select` method that will use query `Params` to filter
    * and project the collection.
    */
  implicit class Select[T: Writes](ts: Iterable[T]) {
    def select[B](params: Params)(sortKey: (T) => B)(implicit ordering: Ordering[B]): SearchResults[JsValue] = {
      import params._

      val results = selectJson(ts, params.query, params.fields, params.max_results)(sortKey).toSeq
      val page = ResultsPage.build(results, PageNumber(page_number.getOrElse(1)), max_results.getOrElse(Int.MaxValue), ItemCount(page_size.getOrElse(50)))
      SearchResults(page.resultsForPage.toList, page.resultCount, page.currentPage.num, page.perPage.count)
    }
  }


}
