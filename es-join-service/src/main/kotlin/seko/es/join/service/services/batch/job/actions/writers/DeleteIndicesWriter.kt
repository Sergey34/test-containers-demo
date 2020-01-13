package seko.es.join.service.services.batch.job.actions.writers

import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestHighLevelClient
import org.springframework.batch.item.ItemWriter
import seko.es.join.service.domain.Item

class DeleteIndicesWriter(
    private val client: RestHighLevelClient
) : ItemWriter<Item> {

    override fun write(items: MutableList<out Item>) {
        items
            .map { DeleteIndexRequest(it.index) }
            .forEach { client.indices().delete(it, RequestOptions.DEFAULT) }
    }
}