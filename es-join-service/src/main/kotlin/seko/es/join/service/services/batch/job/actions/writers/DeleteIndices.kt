package seko.es.join.service.services.batch.job.actions.writers

import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestHighLevelClient
import org.springframework.batch.item.ItemWriter

class DeleteIndices(
    private val client: RestHighLevelClient
) : ItemWriter<Map<String, Any>> {

    override fun write(items: MutableList<out Map<String, Any>>) {
        items
            .map { doc -> DeleteIndexRequest(doc.keys.first()) }
            .forEach { client.indices().delete(it, RequestOptions.DEFAULT) }
    }
}