package seko.es.join.service.services

import org.elasticsearch.action.bulk.BulkRequest
import org.elasticsearch.action.update.UpdateRequest
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestHighLevelClient
import org.springframework.batch.item.ItemWriter
import seko.es.join.service.domain.GlobalConfig
import seko.es.join.service.domain.Writer

class EsItemWriter(
    private val client: RestHighLevelClient,
    private val writerConfig: Writer,
    private val globalConfig: GlobalConfig
) : ItemWriter<Map<*, *>> {

    override fun write(items: MutableList<out Map<*, *>>) {
        val bulkRequest = BulkRequest()
        items
            .map { doc ->
                UpdateRequest()
                    .docAsUpsert(true)
                    .index(globalConfig.targetIndex)
                    .doc(writerConfig.targetField?.let { mapOf(writerConfig.targetField to doc) } ?: doc)
                    .type("doc")
                    .id(doc[writerConfig.id] as String?)
            }
            .forEach { bulkRequest.add(it) }

        val bulk = client.bulk(bulkRequest, RequestOptions.DEFAULT)
        println(bulk)
    }

}