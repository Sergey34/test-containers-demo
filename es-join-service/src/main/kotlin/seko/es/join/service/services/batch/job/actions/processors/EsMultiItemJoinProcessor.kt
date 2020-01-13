package seko.es.join.service.services.batch.job.actions.processors

import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.script.ScriptType
import org.elasticsearch.script.mustache.MultiSearchTemplateRequest
import org.elasticsearch.script.mustache.SearchTemplateRequest
import org.springframework.batch.item.ItemProcessor
import seko.es.join.service.domain.Configuration
import seko.es.join.service.domain.Item
import seko.es.join.service.domain.processors.JoinProcessor
import seko.es.join.service.domain.processors.MultiJoinProcessor

class EsMultiItemJoinProcessor(
    processor: Configuration,
    private val restHighLevelClient: RestHighLevelClient
) : ItemProcessor<Item, Item> {
    private val joinProcessorConfig: MultiJoinProcessor = MultiJoinProcessor.from(processor.config)

    override fun process(item: Item): Item {
        val requests = MultiSearchTemplateRequest()

        val configs: List<JoinProcessor> = joinProcessorConfig.configs
        configs.forEach { jp ->
            val request = SearchTemplateRequest()
            val searchRequest = SearchRequest(jp.index)
            request.request = searchRequest
            request.scriptType = ScriptType.INLINE
            request.script = jp.query
            request.scriptParams = item.content.filterKeys { it in jp.params }
            requests.add(request)
        }

        val response = restHighLevelClient.msearchTemplate(requests, RequestOptions.DEFAULT)

        response.responses.forEachIndexed { index, resp ->
            val doc = resp.response.response.hits.hits.map { it.sourceAsMap }
            item.content[configs[index].target_field] = doc
        }
        return item
    }
}