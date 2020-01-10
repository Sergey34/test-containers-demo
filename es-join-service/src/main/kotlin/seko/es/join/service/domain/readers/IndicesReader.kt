package seko.es.join.service.domain.readers

import com.fasterxml.jackson.annotation.JsonProperty

class IndicesReader(
    @JsonProperty("index")
    val index: List<String> // task*
) {
    companion object {
        @JvmField
        val INDEX_CONFIG_VALIDATOR = { config: Map<String, *> ->
            config["index"] is Array<*>
        }

        fun from(config: Map<String, Any>): IndicesReader {
            return IndicesReader(
                config["index"] as List<String>
            )
        }
    }
}