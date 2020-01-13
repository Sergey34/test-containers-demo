package seko.es.join.service.domain.config.writers

import com.fasterxml.jackson.annotation.JsonProperty
import seko.es.join.service.domain.config.Script

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
                && Script.VALIDATE_SCRIPT(config["script_fields"])
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