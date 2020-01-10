package seko.es.join.service.domain.readers

import com.fasterxml.jackson.annotation.JsonProperty
import seko.es.join.service.domain.Script
import seko.es.join.service.domain.ScriptField
import seko.es.join.service.domain.readers.Order.Companion.ORDER_CONFIG_VALIDATOR

class EsScrollReader(
    @JsonProperty("index")
    val index: String, // task*
    @JsonProperty("fields")
    val fields: List<String> = listOf(),
    @JsonProperty("order")
    val order: Order?,
    @JsonProperty("query")
    val query: String, // {"match_all": {}}
    @JsonProperty("script_fields")
    val scriptFields: List<ScriptField> = listOf(),
    @JsonProperty("time")
    val time: Long = 60_000L
) {
    companion object {
        @JvmField
        val ES_SCROLL_CONFIG_VALIDATOR = { config: Map<String, *> ->
            config["query"] is String
                && config["query"].toString().isNotBlank()
                && (config["fields"] == null || (config["fields"] as List<String>).size > 0)
                && ORDER_CONFIG_VALIDATOR(config["order"] as Map<String, *>?)
                && Script.VALIDATE_SCRIPT(config["script_fields"])
        }

        fun from(config: Map<String, Any>): EsScrollReader {
            val scriptFields = (config["script_fields"] as List<Map<String, Any>>?)
                ?.map { ScriptField.from(it) } ?: listOf()
            return EsScrollReader(
                config["index"] as String,
                config["fields"] as List<String>? ?: listOf(),
                Order.from(config["order"] as Map<String, Any>),
                config["query"] as String,
                scriptFields,
                java.lang.Long.parseLong(config["time"] as String)
            )
        }
    }
}