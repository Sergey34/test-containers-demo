package seko.es.join.service.domain.config.processors

data class MultiJoinProcessor(
    val configs: List<JoinProcessor>
) {
    companion object {
        @JvmField
        val MULTI_JOIN_PROCESSOR_CONFIG_VALIDATOR = { config: Map<String, *> ->
            config["configs"] is List<*>
        }

        fun from(config: Map<String, *>): MultiJoinProcessor {
            return MultiJoinProcessor((config["configs"] as List<Map<String, *>>).map { JoinProcessor.from(it) })
        }
    }
}