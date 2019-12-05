package seko.es.join.service.domain


import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue

data class Script(
        @JsonProperty("lang")
        val lang: String = "painless",
        @JsonProperty("params")
        val params: Map<String, Any>?,
        @JsonProperty("source")
        val source: String // doc['price'].value * 2 * params.factor
) {
    companion object {
        @JvmField
        val objectMapper: ObjectMapper = ObjectMapper().registerModule(KotlinModule())

        fun from(config: Map<String, String>?): Script? {
            return config?.let{ Script(it["lang"] as String, parseParams(it), it["source"] as String) }
        }

        private fun parseParams(config: Map<String, String>): Map<String, Any> {
            return objectMapper.readValue<Map<String, Any>>(config["params"] as String)
        }
    }
}