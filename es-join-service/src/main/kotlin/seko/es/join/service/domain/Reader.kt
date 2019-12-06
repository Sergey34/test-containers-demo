package seko.es.join.service.domain


import com.fasterxml.jackson.annotation.JsonProperty
import org.elasticsearch.search.sort.SortOrder
import java.lang.Long.parseLong

data class Reader(
    @JsonProperty("index")
    val index: String, // task*
    @JsonProperty("type")
    val type: ReaderType, // es
    val config: Map<String, Any>
) {
    enum class ReaderType {
        ES_SCROLL
    }

    class Order(
        val field: String,
        val type: SortOrder
    ) {
        companion object {
            fun from(config: Map<String, Any>): Order {
                return Order(
                    config["field"] as String,
                    SortOrder.valueOf(config["order"] as String)
                )
            }
        }
    }

    class EsScrollReader(
        @JsonProperty("fields")
        val fields: List<String> = listOf(),
        @JsonProperty("order")
        val order: Order?,
        @JsonProperty("query")
        val query: String, // {"match_all": {}}
        @JsonProperty("script_fields")
        val scriptFields: List<ScriptField> = listOf(),
        @JsonProperty("time")
        val time: Long = 60_000L
    ) {
        companion object {
            fun from(config: Map<String, Any>): EsScrollReader {
                val scriptFields = (config["script_fields"] as List<Map<String, Any>>?)
                    ?.map { ScriptField.from(it) } ?: listOf()
                return EsScrollReader(
                    config["fields"] as List<String>? ?: listOf(),
                    Order.from(config["order"] as Map<String, Any>),
                    config["query"] as String,
                    scriptFields,
                    parseLong(config["time"] as String)
                )
            }
        }
    }
}