package seko.es.join.service.domain


import com.fasterxml.jackson.annotation.JsonProperty

data class StepConfig(
    @JsonProperty("chunkSize")
    val chunkSize: Int = 1000, // 100
    @JsonProperty("id")
    val id: String, // tasks
    @JsonProperty("processors")
    val processors: List<Processor>?,
    @JsonProperty("reader")
    val reader: Reader,
    @JsonProperty("writer")
    val writer: Writer
)