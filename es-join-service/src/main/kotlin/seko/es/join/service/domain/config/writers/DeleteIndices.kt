package seko.es.join.service.domain.config.writers

class DeleteIndices {
    companion object {
        @JvmField
        val ES_DELETE_INDICES_CONFIG_VALIDATOR = { config: Map<String, *> ->
            true
        }

        fun from(config: Map<String, Any>): DeleteIndices {
            return DeleteIndices()
        }
    }
}