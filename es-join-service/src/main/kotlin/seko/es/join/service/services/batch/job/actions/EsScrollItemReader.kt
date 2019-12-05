package seko.es.join.service.services.batch.job.actions

import org.elasticsearch.action.search.ClearScrollRequest
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.action.search.SearchScrollRequest
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.common.unit.TimeValue
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.script.Script
import org.elasticsearch.script.ScriptType
import org.elasticsearch.search.Scroll
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.elasticsearch.search.sort.SortOrder
import org.springframework.batch.item.data.AbstractPaginatedDataItemReader
import seko.es.join.service.domain.Reader
import seko.es.join.service.domain.ScriptField
import java.lang.Long.parseLong

class EsScrollItemReader(
        private val restHighLevelClient: RestHighLevelClient,
        private val readerConfig: Reader,
        private val chunkSize: Int
) : AbstractPaginatedDataItemReader<Map<String, Any>>() {
    private var scrollId: String? = null
    private lateinit var searchRequest: SearchRequest

    override fun doOpen() {
        searchRequest = SearchRequest(readerConfig.index)
        val searchSourceBuilder = SearchSourceBuilder()
        searchSourceBuilder.size(chunkSize)
        searchSourceBuilder.query(QueryBuilders.wrapperQuery(readerConfig.config["query"] as String))

        (readerConfig.config["fields"] as List<String>?)?.let {
            searchSourceBuilder.fetchSource(it.toTypedArray(), null)
        }
        (readerConfig.config["order"] as Map<String, String>?)?.let {
            searchSourceBuilder.sort(it["field"], SortOrder.fromString(it["type"]))
        }
        (readerConfig.config["script_fields"] as List<Map<String, Any>>?)
                ?.map { ScriptField.from(it) }
                ?.forEach {
                    val script = it.script.params
                            ?.let { p -> Script(ScriptType.INLINE, it.script.lang, it.script.source, p) }
                            ?: Script(ScriptType.INLINE, it.script.lang, it.script.source, mapOf())
                    searchSourceBuilder.scriptField(it.fieldName, script)
                }
        searchRequest.source(searchSourceBuilder)

        val scroll = Scroll(TimeValue.timeValueMillis(parseLong(readerConfig.config["time"] as String)))
        searchRequest.scroll(scroll)
    }

    override fun doPageRead(): Iterator<Map<String, Any>> {
        val searchResponse: SearchResponse
        if (scrollId == null) {
            searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT)
            scrollId = searchResponse.scrollId
        } else {
            val scrollRequest = SearchScrollRequest(scrollId)
            scrollRequest.scroll(searchRequest.scroll())
            searchResponse = restHighLevelClient.scroll(scrollRequest, RequestOptions.DEFAULT)
            scrollId = searchResponse.scrollId
        }
        val searchHits = searchResponse.hits.hits.map { it.sourceAsMap }
        return searchHits.iterator()
    }

    override fun doClose() {
        if (scrollId != null) {
            try {
                val clearScrollRequest = ClearScrollRequest()
                clearScrollRequest.addScrollId(scrollId)
                restHighLevelClient.clearScroll(clearScrollRequest, RequestOptions.DEFAULT)
            } finally {
                scrollId = null
            }
        }
    }
}