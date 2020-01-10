package seko.es.join.service.services.batch.job.actions.readers

import org.elasticsearch.action.admin.indices.get.GetIndexRequest
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestHighLevelClient
import org.springframework.batch.item.data.AbstractPaginatedDataItemReader
import seko.es.join.service.domain.Configuration
import seko.es.join.service.domain.readers.IndicesReader


class IndicesReader(
    private val restHighLevelClient: RestHighLevelClient,
    readerConfig: Configuration
) : AbstractPaginatedDataItemReader<MutableMap<String, Any>>() {
    private val config = IndicesReader.from(readerConfig.config)
    private lateinit var request: GetIndexRequest
    private var executed = false

    override fun doOpen() {
        request = GetIndexRequest().indices(*config.index.toTypedArray())
    }

    override fun doPageRead(): Iterator<MutableMap<String, Any>> {
        if (!executed) {
            val get = restHighLevelClient.indices().get(request, RequestOptions.DEFAULT)
            val result = get.settings.map { mutableMapOf(it.key!! to it.value.asGroups as Any) }
            executed = true
            return result.iterator()
        }

        return listOf<MutableMap<String, Any>>().iterator()
    }

    override fun doClose() {
    }
}