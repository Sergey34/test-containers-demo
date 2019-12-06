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

    data class JoinProcessor(
        @JsonProperty("type")
        val type: String,
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
            fun from(config: Map<String, *>): JoinProcessor {
                return JoinProcessor(
                    config["type"] as String,
                    config["index"] as String,
                    config["query"] as String,
                    config["target_field"] as String,
                    config["size"] as Int,
                    config["params"] as List<String>
                )
            }
        }
    }

    enum class ProcessorType {
        JOIN, JS
    }
}