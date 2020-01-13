package seko.es.join.service.domain.config.writers

class DeleteDocument {
    companion object {
        @JvmField
        val ES_DELETE_DOCUMENTS_CONFIG_VALIDATOR = { config: Map<String, *> ->
            true
        }

        fun from(config: Map<String, Any>): DeleteIndices {
            return DeleteIndices()
        }
    }
}