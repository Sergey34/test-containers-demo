package seko.es.join.service.domain.writers

class DeleteIndices {
    companion object {
        @JvmField
        val ES_DELETE_CONFIG_VALIDATOR = { config: Map<String, *> ->
            true
        }

        fun from(config: Map<String, Any>): DeleteIndices {
            return DeleteIndices()
        }
    }
}