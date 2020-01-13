package seko.es.join.service.domain.config.writers

import org.elasticsearch.action.DocWriteRequest

data class EsIndexWriter(
    val opType: DocWriteRequest.OpType = DocWriteRequest.OpType.CREATE,
    val fieldWithDocId: String?
) {
    companion object {
        @JvmField
        val ES_INDEX_WRITER_CONFIG_VALIDATOR = { config: Map<String, *> ->
            config["op_type"] is String
                && config["op_type"].toString().isNotBlank()
                && (config["field_with_doc_id"] == null || (config["field_with_doc_id"].toString().isNotBlank()))
        }

        fun from(config: Map<String, Any>): EsIndexWriter {
            return EsIndexWriter(
                DocWriteRequest.OpType.valueOf(config["op_type"] as String),
                config["field_with_doc_id"] as String?
            )
        }
    }
}