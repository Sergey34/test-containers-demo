package seko.es.join.service.domain


import com.fasterxml.jackson.annotation.JsonProperty

data class Reader(
    @JsonProperty("fields")
    val fields: List<String> = listOf(),
    @JsonProperty("index")
    val index: String, // task*
    @JsonProperty("order")
    val order: Order?,
    @JsonProperty("query")
    val query: String, // {"match_all": {}}
    @JsonProperty("script_fields")
    val scriptFields: List<ScriptField> = listOf(),
    @JsonProperty("type")
    val type: ReaderType // es
) {
    enum class ReaderType {
        ES_SCROLL
    }

    class Order(
        val field: String,
        val type: OrderType
    ) {
        enum class OrderType {
            ASK, DESK
        }
    }
}