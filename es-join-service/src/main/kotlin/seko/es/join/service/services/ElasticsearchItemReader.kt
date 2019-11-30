package seko.es.join.service.services

import org.elasticsearch.action.search.ClearScrollRequest
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.action.search.SearchScrollRequest
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.common.unit.TimeValue
import org.elasticsearch.search.Scroll
import org.springframework.batch.item.data.AbstractPaginatedDataItemReader


class ElasticsearchItemReader(
    private val restHighLevelClient: RestHighLevelClient,
    private val searchRequest: SearchRequest
) : AbstractPaginatedDataItemReader<Map<*, *>>() {
    private var scrollId: String? = null
    private val scrollTimeinMillis = 1L


    override fun doPageRead(): Iterator<Map<*, *>> {
        val scroll = Scroll(TimeValue.timeValueMinutes(scrollTimeinMillis))
        searchRequest.scroll(scroll)

        val searchResponse: SearchResponse
        if (scrollId == null) {
            searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT)
            scrollId = searchResponse.scrollId
        } else {
            val scrollRequest = SearchScrollRequest(scrollId)
            scrollRequest.scroll(scroll)
            searchResponse = restHighLevelClient.scroll(scrollRequest, RequestOptions.DEFAULT)
            scrollId = searchResponse.scrollId
        }
        val searchHits = searchResponse.hits.hits.map { it.sourceAsMap }
        return searchHits.iterator()
    }

    override fun doClose() {
        if (scrollId != null) {
            val clearScrollRequest = ClearScrollRequest()
            clearScrollRequest.addScrollId(scrollId)
            restHighLevelClient.clearScroll(clearScrollRequest, RequestOptions.DEFAULT)
            scrollId = null
        }
    }
}