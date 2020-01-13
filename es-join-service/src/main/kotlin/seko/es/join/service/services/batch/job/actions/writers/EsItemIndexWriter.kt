package seko.es.join.service.services.batch.job.actions.writers

import org.elasticsearch.action.bulk.BulkRequest
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestHighLevelClient
import org.springframework.batch.item.ItemWriter
import seko.es.join.service.domain.Item
import seko.es.join.service.domain.config.Configuration
import seko.es.join.service.domain.config.GlobalConfig
import seko.es.join.service.domain.config.writers.EsIndexWriter


class EsItemIndexWriter(
    private val client: RestHighLevelClient,
    writerConfig: Configuration,
    private val globalConfig: GlobalConfig
) : ItemWriter<Item>, BulkValidatable {
    private val indexWriterConfig: EsIndexWriter = EsIndexWriter.from(writerConfig.config)

    override fun write(items: MutableList<out Item>) {
        val bulkRequest = BulkRequest()
        items
            .map { doc ->
                IndexRequest(globalConfig.targetIndex)
                    .source(doc.content)
                    .opType(indexWriterConfig.opType)
                    .apply {
                        if (indexWriterConfig.fieldWithDocId != null) {
                            id(doc.content[indexWriterConfig.fieldWithDocId] as String?)
                        }
                    }
            }
            .forEach { bulkRequest.add(it) }

        client.bulk(bulkRequest, RequestOptions.DEFAULT)
            .apply {
                validate(this)
            }
    }
}