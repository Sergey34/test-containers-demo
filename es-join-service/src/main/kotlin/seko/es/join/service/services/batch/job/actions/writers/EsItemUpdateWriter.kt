package seko.es.join.service.services.batch.job.actions.writers

import org.elasticsearch.action.bulk.BulkRequest
import org.elasticsearch.action.update.UpdateRequest
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.script.Script
import org.elasticsearch.script.ScriptType
import org.springframework.batch.item.ItemWriter
import seko.es.join.service.domain.Item
import seko.es.join.service.domain.config.Configuration
import seko.es.join.service.domain.config.GlobalConfig
import seko.es.join.service.domain.config.writers.EsUpdateWriter
import seko.es.join.service.services.constants.EsConstants.Companion.DOC_TYPE

class EsItemUpdateWriter(
    private val client: RestHighLevelClient,
    writerConfig: Configuration,
    private val globalConfig: GlobalConfig
) : ItemWriter<Item> {


    private val updateWriterConfig: EsUpdateWriter = EsUpdateWriter.from(writerConfig.config)

    override fun write(items: MutableList<out Item>) {
        val bulkRequest = BulkRequest()
        items
            .map { doc ->
                val script = updateWriterConfig.script?.let {
                    Script(ScriptType.INLINE, Script.DEFAULT_SCRIPT_LANG, it.source, it.params)
                }
                val esDoc = updateWriterConfig.targetField
                    ?.let {
                        mapOf(updateWriterConfig.targetField to doc)
                    } ?: doc.content
                UpdateRequest()
                    .docAsUpsert(updateWriterConfig.docAsUpsert)
                    .script(script)
                    .retryOnConflict(updateWriterConfig.retryOnConflict)
                    .index(globalConfig.targetIndex)
                    .doc(esDoc)
                    .type(DOC_TYPE)
                    .id(doc.content[updateWriterConfig.fieldWithDocId] as String?)
            }
            .forEach { bulkRequest.add(it) }

        val bulk = client.bulk(bulkRequest, RequestOptions.DEFAULT)
    }

}