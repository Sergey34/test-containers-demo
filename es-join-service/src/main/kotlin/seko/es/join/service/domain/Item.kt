package seko.es.join.service.domain

import seko.es.join.service.services.constants.EsConstants.Companion.DOC_TYPE

data class Item(
    val index: String,
    val type: String = DOC_TYPE,
    val id: String = "",
    val content: MutableMap<String, Any?>
)