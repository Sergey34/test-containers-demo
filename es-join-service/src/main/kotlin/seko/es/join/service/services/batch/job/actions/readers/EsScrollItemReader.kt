package seko.es.join.service.services.batch.job.actions.readers

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
import org.springframework.batch.item.data.AbstractPaginatedDataItemReader
import seko.es.join.service.domain.Configuration
import seko.es.join.service.domain.readers.EsScrollReader

class EsScrollItemReader(
    private val restHighLevelClient: RestHighLevelClient,
    readerConfig: Configuration,
    private val chunkSize: Int
) : AbstractPaginatedDataItemReader<MutableMap<String, Any>>() {
    private val config = EsScrollReader.from(readerConfig.config)

    private var scrollId: String? = null
    private lateinit var searchRequest: SearchRequest

    override fun doOpen() {
        searchRequest = SearchRequest(config.index)
        val searchSourceBuilder = SearchSourceBuilder()
        searchSourceBuilder.size(chunkSize)
        searchSourceBuilder.query(QueryBuilders.wrapperQuery(config.query))

        config.fields.let {
            searchSourceBuilder.fetchSource(it.toTypedArray(), null)
        }
        config.order?.let {
            searchSourceBuilder.sort(it.field, it.type)
        }
        config.scriptFields.forEach {
            val script = it.script.params
                ?.let { p -> Script(ScriptType.INLINE, it.script.lang, it.script.source, p) }
                ?: Script(ScriptType.INLINE, it.script.lang, it.script.source, mapOf())
            searchSourceBuilder.scriptField(it.fieldName, script)
        }
        searchRequest.source(searchSourceBuilder)

        val scroll = Scroll(TimeValue.timeValueMillis(config.time))
        searchRequest.scroll(scroll)
    }

    override fun doPageRead(): Iterator<MutableMap<String, Any>> {
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
        val searchHits = searchResponse.hits.hits.map {
            it.sourceAsMap.apply {
                it.fields.values.forEach { field ->
                    put(field.name, field.values)
                }
            }
        }
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