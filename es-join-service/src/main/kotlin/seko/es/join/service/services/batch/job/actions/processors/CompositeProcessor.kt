package seko.es.join.service.services.batch.job.actions.processors

import org.springframework.batch.item.ItemProcessor

class CompositeProcessor constructor(
    private val processors: List<ItemProcessor<MutableMap<String, Any>, Map<String, Any>>>
) : ItemProcessor<MutableMap<String, Any>, Map<String, Any>> {
    override fun process(item: MutableMap<String, Any>): Map<String, Any>? {
        processors.forEach { it.process(item) }
        return item
    }
}