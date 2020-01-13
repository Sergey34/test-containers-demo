package seko.es.join.service.domain.config.processors

import com.fasterxml.jackson.annotation.JsonProperty

data class FilterProcessor(
    @JsonProperty("script")
    val script: String
) {
    companion object {
        @JvmField
        val SCRIPT_PROCESSOR_CONFIG_VALIDATOR = { config: Map<String, *> ->
            config["script"] is String
        }

        fun from(config: Map<String, *>): FilterProcessor {
            return FilterProcessor(config["script"] as String)
        }
    }
}
