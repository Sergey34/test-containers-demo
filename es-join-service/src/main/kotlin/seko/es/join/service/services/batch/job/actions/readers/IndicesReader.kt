package seko.es.join.service.services.batch.job.actions.readers

import org.elasticsearch.action.admin.indices.get.GetIndexRequest
import org.elasticsearch.action.admin.indices.get.GetIndexResponse
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestHighLevelClient
import org.springframework.batch.item.data.AbstractPaginatedDataItemReader
import seko.es.join.service.domain.Item
import seko.es.join.service.domain.config.Configuration
import seko.es.join.service.domain.config.readers.IndicesReader


class IndicesReader(
    private val restHighLevelClient: RestHighLevelClient,
    readerConfig: Configuration
) : AbstractPaginatedDataItemReader<Item>() {
    private val config = IndicesReader.from(readerConfig.config)
    private lateinit var request: GetIndexRequest
    private var executed = false

    override fun doOpen() {
        request = GetIndexRequest().indices(*config.index.toTypedArray())
    }

    override fun doPageRead(): Iterator<Item> {
        if (!executed) {
            val get: GetIndexResponse = restHighLevelClient.indices().get(request, RequestOptions.DEFAULT)
            val result = get.settings.map {
                Item(
                    index = it.key,
                    content = it.value.asGroups.toMutableMap()
                )
            }
            executed = true
            return result.iterator()
        }

        return listOf<Item>().iterator()
    }

    override fun doClose() {
    }
}