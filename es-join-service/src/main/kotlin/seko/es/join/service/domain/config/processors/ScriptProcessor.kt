package seko.es.join.service.domain.config.processors

import com.fasterxml.jackson.annotation.JsonProperty

data class ScriptProcessor(
    @JsonProperty("script")
    val script: String
) {
    companion object {
        @JvmField
        val JS_PROCESSOR_CONFIG_VALIDATOR = { config: Map<String, *> ->
            config["script"] is String
        }

        fun from(config: Map<String, *>): ScriptProcessor {
            return ScriptProcessor(config["script"] as String)
        }
    }
}