package seko.es.join.service.domain


import com.fasterxml.jackson.annotation.JsonProperty

data class Script(
    @JsonProperty("lang")
    val lang: String = "painless",
    @JsonProperty("params")
    val params: String?, // {"factor": 2.0}
    @JsonProperty("source")
    val source: String // doc['price'].value * 2 * params.factor
)