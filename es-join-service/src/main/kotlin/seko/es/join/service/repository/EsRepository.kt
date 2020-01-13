package seko.es.join.service.repository

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.elasticsearch.action.get.GetRequest
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.action.search.*
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.common.unit.TimeValue
import org.elasticsearch.common.xcontent.XContentType
import org.elasticsearch.index.query.QueryBuilders.matchAllQuery
import org.elasticsearch.rest.RestStatus
import org.elasticsearch.search.Scroll
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.elasticsearch.search.slice.SliceBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository
import seko.es.join.service.domain.config.JobConfig
import seko.es.join.service.services.config.parsing.JobConfigParser
import seko.es.join.service.services.constants.EsConstants.Companion.DOC_TYPE
import java.io.File


@Repository
class EsRepository @Autowired constructor(
    private val restHighLevelClient: RestHighLevelClient,
    private val objectMapper: ObjectMapper,
    private val jobConfigParser: JobConfigParser
) {

    fun getConfig(slice: SliceBuilder): List<JobConfig> {
        val scroll = Scroll(TimeValue.timeValueMinutes(1L))
        val searchRequest = SearchRequest(".join")
        searchRequest.scroll(scroll)
        val searchSourceBuilder = SearchSourceBuilder()
        searchSourceBuilder.query(matchAllQuery())
        searchSourceBuilder.slice(slice)
        searchRequest.source(searchSourceBuilder)

        var searchResponse: SearchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT)
        var scrollId = searchResponse.scrollId
        var searchHits = searchResponse.hits.hits

        val configs = mutableListOf<JobConfig>()
        while (searchHits != null && searchHits.isNotEmpty()) {
            configs.addAll(searchHits.map { jobConfigParser.parse(it.sourceAsString) })
            val scrollRequest = SearchScrollRequest(scrollId)
            scrollRequest.scroll(scroll)
            searchResponse = restHighLevelClient.scroll(scrollRequest, RequestOptions.DEFAULT)
            scrollId = searchResponse.scrollId
            searchHits = searchResponse.hits.hits
        }

        try {
            val clearScrollRequest = ClearScrollRequest()
            clearScrollRequest.addScrollId(scrollId)
            val clearScrollResponse: ClearScrollResponse =
                restHighLevelClient.clearScroll(clearScrollRequest, RequestOptions.DEFAULT)
            val succeeded = clearScrollResponse.isSucceeded
        } finally {
            return configs
        }
    }

    fun getJobs(): List<JobConfig> {
        return objectMapper.readValue(File("t3.json").reader())
    }

    fun save(jobConfig: JobConfig): RestStatus {
        val request = IndexRequest(".join")
            .id(jobConfig.jobId)
            .type(DOC_TYPE)
            .source(objectMapper.writeValueAsString(jobConfig), XContentType.JSON)
        val index = restHighLevelClient.index(request, RequestOptions.DEFAULT)
        return index.status()
    }

    fun getJob(jobId: String): JobConfig {
        return jobConfigParser.parse(
            restHighLevelClient.get(
                GetRequest(".join", DOC_TYPE, jobId),
                RequestOptions.DEFAULT
            ).sourceAsString
        )
    }
}