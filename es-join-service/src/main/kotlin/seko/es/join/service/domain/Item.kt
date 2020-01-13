package seko.es.join.service.domain

data class Item(
    val index: String,
    val type: String,
    val id: String,
    val content: MutableMap<String, Any?>
)