package seko.es.join.service.services.batch.job.actions.processors

import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.script.ScriptType
import org.elasticsearch.script.mustache.SearchTemplateRequest
import org.springframework.batch.item.ItemProcessor
import seko.es.join.service.domain.Processor


class EsItemJoinProcessor(
        processor: Processor,
        private val restHighLevelClient: RestHighLevelClient
) : ItemProcessor<MutableMap<String, Any>, Map<String, Any>> {
    private val joinProcessorConfig: Processor.JoinProcessor = Processor.JoinProcessor.from(processor.config)

    override fun process(item: MutableMap<String, Any>): Map<String, Any> {
        val request = SearchTemplateRequest()
        request.request = SearchRequest(joinProcessorConfig.index)
        request.scriptType = ScriptType.INLINE
        request.script = joinProcessorConfig.query
        request.scriptParams = item.filterKeys { it in joinProcessorConfig.params }

        item[joinProcessorConfig.target_field] = restHighLevelClient.searchTemplate(request, RequestOptions.DEFAULT).response.hits.hits
        return item
    }
}