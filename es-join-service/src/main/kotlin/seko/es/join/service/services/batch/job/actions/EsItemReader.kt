package seko.es.join.service.services.batch.job.actions

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
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
import seko.es.join.service.domain.Reader

class EsItemReader(
    private val restHighLevelClient: RestHighLevelClient,
    private val readerConfig: Reader,
    private val chunkSize: Int
) : AbstractPaginatedDataItemReader<Map<String, Any>>() {
    private var scrollId: String? = null
    private lateinit var searchRequest: SearchRequest
    private val mapper: ObjectMapper = ObjectMapper().registerModule(KotlinModule())

    override fun doOpen() {
        /*searchRequest = SearchRequest(readerConfig.index)
        val searchSourceBuilder = SearchSourceBuilder()
        searchSourceBuilder.size(chunkSize)
        searchSourceBuilder.query(QueryBuilders.wrapperQuery(readerConfig.query))
        readerConfig.fields?.let {
            searchSourceBuilder.fetchSource(it.toTypedArray(), null)
        }
        readerConfig.order?.let {
            searchSourceBuilder.sort(it.field, it.type)
        }
        readerConfig.scriptFields.forEach {
            val script = (it.script.params?.let { p -> mapper.readValue<Map<String, Any>>(p) }
                ?.let { p -> Script(ScriptType.INLINE, it.script.lang, it.script.source, p) }
                ?: Script(ScriptType.INLINE, it.script.lang, it.script.source, mapOf()))
            searchSourceBuilder.scriptField(it.fieldName, script)
        }
        searchRequest.source(searchSourceBuilder)

        val scroll = Scroll(TimeValue.timeValueMillis(readerConfig.time))
        searchRequest.scroll(scroll)*/
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
            val clearScrollRequest = ClearScrollRequest()
            clearScrollRequest.addScrollId(scrollId)
            restHighLevelClient.clearScroll(clearScrollRequest, RequestOptions.DEFAULT)
            scrollId = null
        }
    }
}