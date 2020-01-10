package seko.es.join.service.services.batch.job.actions.processors

import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.script.ScriptType
import org.elasticsearch.script.mustache.SearchTemplateRequest
import org.springframework.batch.item.ItemProcessor
import seko.es.join.service.domain.Configuration
import seko.es.join.service.domain.processors.JoinProcessor


class EsItemJoinProcessor(
    processor: Configuration,
    private val restHighLevelClient: RestHighLevelClient
) : ItemProcessor<MutableMap<String, Any>, Map<String, Any>> {
    private val joinProcessorConfig: JoinProcessor = JoinProcessor.from(processor.config)

    override fun process(item: MutableMap<String, Any>): Map<String, Any> {
        val request = SearchTemplateRequest()
        val searchRequest = SearchRequest(joinProcessorConfig.index)

        request.request = searchRequest
        request.scriptType = ScriptType.INLINE
        request.script = joinProcessorConfig.query
        request.scriptParams = item.filterKeys { it in joinProcessorConfig.params }


        item[joinProcessorConfig.target_field] =
            restHighLevelClient.searchTemplate(request, RequestOptions.DEFAULT)
                .response.hits.hits.map { it.sourceAsMap }
        return item
    }
}