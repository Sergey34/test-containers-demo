package seko.es.join.service.domain


import com.fasterxml.jackson.annotation.JsonProperty
import org.elasticsearch.search.sort.SortOrder

data class Reader(
    @JsonProperty("fields")
    val fields: List<String>?,
    @JsonProperty("index")
    val index: String, // task*
    @JsonProperty("order")
    val order: Order?,
    @JsonProperty("query")
    val query: String, // {"match_all": {}}
    @JsonProperty("script_fields")
    val scriptFields: List<ScriptField> = listOf(),
    @JsonProperty("type")
    val type: ReaderType, // es
    @JsonProperty("time")
    val time: Long = 60_000L
) {
    enum class ReaderType {
        ES_SCROLL
    }

    class Order(
        val field: String,
        val type: SortOrder
    )
}