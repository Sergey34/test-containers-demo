package seko.es.join.service.services

import org.elasticsearch.action.bulk.BulkRequest
import org.elasticsearch.action.update.UpdateRequest
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestHighLevelClient
import org.springframework.batch.item.ItemWriter

class EsItemWriter(
    private val client: RestHighLevelClient
) : ItemWriter<Map<*, *>> {

    override fun write(items: MutableList<out Map<*, *>>) {
        val bulkRequest = BulkRequest()
        items
            .map { UpdateRequest().docAsUpsert(true).index("witrina").doc(it).type("dco").id(it["field_3"] as String) }
            .forEach { bulkRequest.add(it) }

        val bulk = client.bulk(bulkRequest, RequestOptions.DEFAULT)
        println(bulk)
    }

}