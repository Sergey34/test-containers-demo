package seko.es.join.service.domain


import com.fasterxml.jackson.annotation.JsonProperty

data class Processor(
        @JsonProperty("type")
        val type: ProcessorType, // groovy
        @JsonProperty("config")
        val config: Map<String, *>
) {
    data class ScriptProcessor(
            @JsonProperty("script")
            val script: String
    ) {
        companion object {
            fun from(config: Map<String, *>): ScriptProcessor {
                return ScriptProcessor(config["script"] as String)
            }
        }
    }

    enum class ProcessorType {
        GROOVY, JS
    }
}