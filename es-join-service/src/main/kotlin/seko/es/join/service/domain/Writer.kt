package seko.es.join.service.domain


import com.fasterxml.jackson.annotation.JsonProperty
import org.elasticsearch.action.DocWriteRequest
import seko.es.join.service.domain.Script.Companion.VALIDATE_SCRIPT

data class Writer(
    @JsonProperty("config")
    val config: Map<String, Any>,
    @JsonProperty("type")
    val type: WriterType
) {
    class EsUpdateWriter(
        @JsonProperty("script")
        val script: Script?,
        @JsonProperty("doc_as_upsert")
        val docAsUpsert: Boolean = true,
        @JsonProperty("retry_on_conflict")
        val retryOnConflict: Int = 0,
        @JsonProperty("target_field")
        val targetField: String?,
        @JsonProperty("field_with_doc_id")
        val fieldWithDocId: String
    ) {
        companion object {
            @JvmField
            val ES_UPDATE_WRITER_CONFIG_VALIDATOR = { config: Map<String, *> ->
                config["field_with_doc_id"] is String
                        && config["field_with_doc_id"].toString().isNotBlank()
                        && (config["target_field"] == null || (config["target_field"].toString().isNotBlank()))
                        && (config["retry_on_conflict"] == null || config["retry_on_conflict"] is Int)
                        && (config["doc_as_upsert"] == null || config["doc_as_upsert"] is Boolean)
                        && VALIDATE_SCRIPT(config["script_fields"])
            }

            fun from(config: Map<String, Any>): EsUpdateWriter {
                return EsUpdateWriter(
                    Script.from(config["script"] as Map<String, String>?),
                    config["doc_as_upsert"] as Boolean,
                    config["retry_on_conflict"] as Int,
                    config["target_field"] as String?,
                    config["field_with_doc_id"] as String
                )
            }
        }
    }

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

    enum class WriterType {
        UPDATE_BY_QUERY, UPDATE, UPDATE_BY_SCRIPT, INDEX
    }
}