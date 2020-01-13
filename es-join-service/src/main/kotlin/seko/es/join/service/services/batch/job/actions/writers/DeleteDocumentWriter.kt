package seko.es.join.service.services.batch.job.actions.writers

import org.elasticsearch.action.bulk.BulkRequest
import org.elasticsearch.action.delete.DeleteRequest
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestHighLevelClient
import org.springframework.batch.item.ItemWriter
import seko.es.join.service.domain.Item

class DeleteDocumentWriter(
    private val client: RestHighLevelClient
) : ItemWriter<Item>, BulkValidatable {

    override fun write(items: MutableList<out Item>) {
        val bulkRequest = BulkRequest()
        items
            .map { doc ->
                DeleteRequest(doc.index, doc.type, doc.id)
            }
            .forEach { bulkRequest.add(it) }

        client.bulk(bulkRequest, RequestOptions.DEFAULT)
            .apply {
                validate(this)
            }
    }
}