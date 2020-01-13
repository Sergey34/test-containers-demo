package seko.es.join.service.domain.config


import com.fasterxml.jackson.annotation.JsonAnySetter
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class Configuration(
    @JsonProperty("type")
    val type: String,
    val config: MutableMap<String, Any> = mutableMapOf()
) {
    @JsonAnySetter
    fun fillConfig(name: String, value: Map<String, Any>) {
        this.config.putAll(value)
    }
}