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
            @JvmField
            val JS_PROCESSOR_CONFIG_VALIDATOR = { config: Map<String, *> ->
                config["script"] is String && config["field_with_doc_id"].toString().isNotBlank()
            }

            fun from(config: Map<String, *>): ScriptProcessor {
                return ScriptProcessor(config["script"] as String)
            }
        }
    }

    data class MultiJoinProcessor(
        val configs: List<JoinProcessor>
    ) {
        companion object {
            @JvmField
            val MULTI_JOIN_PROCESSOR_CONFIG_VALIDATOR = { config: Map<String, *> ->
                config["configs"] is List<*>
            }

            fun from(config: Map<String, *>): MultiJoinProcessor {
                return MultiJoinProcessor((config["configs"] as List<Map<String, *>>).map { JoinProcessor.from(it) })
            }
        }
    }

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

    enum class ProcessorType {
        MULTI_JOIN, JOIN, JS
    }
}