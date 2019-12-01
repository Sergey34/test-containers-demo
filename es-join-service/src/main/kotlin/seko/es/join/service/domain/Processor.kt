package seko.es.join.service.domain


import com.fasterxml.jackson.annotation.JsonProperty

data class Processor(
    @JsonProperty("script")
    val script: String, // source.status = source.status_history.find {it.status in ['actual', 'handling']}
    @JsonProperty("type")
    val type: ProcessorType // groovy
) {
    enum class ProcessorType {
        GROOVY, JS
    }
}