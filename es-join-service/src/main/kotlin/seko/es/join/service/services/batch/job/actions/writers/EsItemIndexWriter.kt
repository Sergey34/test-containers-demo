package seko.es.join.service.services.batch.job.actions.writers

import org.elasticsearch.action.bulk.BulkRequest
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestHighLevelClient
import org.springframework.batch.item.ItemWriter
import seko.es.join.service.domain.GlobalConfig
import seko.es.join.service.domain.Writer


class EsItemIndexWriter(
    private val client: RestHighLevelClient,
    writerConfig: Writer,
    private val globalConfig: GlobalConfig
) : ItemWriter<Map<String, Any>> {

    private val indexWriterConfig: Writer.EsIndexWriter = Writer.EsIndexWriter.from(writerConfig.config)

    override fun write(items: MutableList<out Map<String, Any>>) {
        val bulkRequest = BulkRequest()
        items
            .map { doc ->
                IndexRequest(globalConfig.targetIndex)
                    .source(doc)
                    .opType(indexWriterConfig.opType)
                    .apply {
                        if (indexWriterConfig.fieldWithDocId != null) {
                            id(doc[indexWriterConfig.fieldWithDocId] as String?)
                        }
                    }
            }
            .forEach { bulkRequest.add(it) }

        val bulk = client.bulk(bulkRequest, RequestOptions.DEFAULT)
        println(bulk)
    }

}