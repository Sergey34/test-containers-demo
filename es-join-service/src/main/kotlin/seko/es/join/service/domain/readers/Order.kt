package seko.es.join.service.domain.readers

import org.elasticsearch.search.sort.SortOrder

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