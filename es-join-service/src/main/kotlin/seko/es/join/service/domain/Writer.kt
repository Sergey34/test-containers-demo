package seko.es.join.service.domain


import com.fasterxml.jackson.annotation.JsonProperty

data class Writer(
    @JsonProperty("conflicts")
    val conflicts: String?, // proceed
    @JsonProperty("query")
    val query: String?, // {"match_all": {}}
    @JsonProperty("routing")
    val routing: Int, // 1000
    @JsonProperty("script")
    val script: Script?,
    @JsonProperty("scroll_size")
    val scrollSize: Int?, // 1000
    @JsonProperty("slices")
    val slices: Int?, // 5
    @JsonProperty("type")
    val type: WriterType, // updateByQuery
    @JsonProperty("id")
    val id: String?,
    @JsonProperty("target_field")
    val targetField: String?
) {
    enum class WriterType {
        UPDATE_BY_QUERY, UPDATE, UPDATE_BY_SCRIPT
    }
}