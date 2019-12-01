package seko.es.join.service.domain


import com.fasterxml.jackson.annotation.JsonProperty

data class Step(
    @JsonProperty("chunkSize")
    val chunkSize: Int, // 100
    @JsonProperty("id")
    val id: String, // tasks
    @JsonProperty("processor")
    val processor: Processor?,
    @JsonProperty("reader")
    val reader: Reader,
    @JsonProperty("writer")
    val writer: Writer
)