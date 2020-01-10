package seko.es.join.service.domain.processors

import com.fasterxml.jackson.annotation.JsonProperty

data class JoinProcessor(
    @JsonProperty("index")
    val index: String,
    @JsonProperty("query")
    val query: String,
    @JsonProperty("target_field")
    val target_field: String,
    val size: Int = 10,
    val params: List<String>
) {
    companion object {
        @JvmField
        val JOIN_PROCESSOR_CONFIG_VALIDATOR = { config: Map<String, *> ->
            config["index"] is String
                && config["query"] is String
                && config["target_field"] is String
                && (config["size"] == null || config["size"] is Int)
                && (config["params"] as List<String>).size > 0
        }

        fun from(config: Map<String, *>): JoinProcessor {
            return JoinProcessor(
                config["index"] as String,
                config["query"] as String,
                config["target_field"] as String,
                (config["size"] as Int?) ?: 10,
                config["params"] as List<String>
            )
        }
    }
}
