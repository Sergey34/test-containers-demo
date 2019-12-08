package seko.es.join.service.domain


import com.fasterxml.jackson.annotation.JsonProperty
import org.elasticsearch.search.sort.SortOrder
import seko.es.join.service.domain.Reader.Order.Companion.ORDER_CONFIG_VALIDATOR
import seko.es.join.service.domain.Script.Companion.VALIDATE_SCRIPT
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
            @JvmField
            val ORDER_CONFIG_VALIDATOR = { config: Map<String, *>? ->
                config == null || (config is Map<*, *> && (config as Map<*, *>).keys.containsAll(
                    listOf("field", "type")
                ))
            }

            fun from(config: Map<String, Any>): Order {
                return Order(
                    config["field"] as String,
                    SortOrder.valueOf(config["type"] as String)
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
            @JvmField
            val ES_SCROLL_CONFIG_VALIDATOR = { config: Map<String, *> ->
                config["query"] is String
                        && config["query"].toString().isNotBlank()
                        && (config["fields"] == null || (config["fields"] as List<String>).size > 0)
                        && ORDER_CONFIG_VALIDATOR(config["order"] as Map<String, *>?)
                        && VALIDATE_SCRIPT(config["script_fields"])
            }

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