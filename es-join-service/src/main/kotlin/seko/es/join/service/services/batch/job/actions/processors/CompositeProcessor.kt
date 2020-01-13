package seko.es.join.service.services.batch.job.actions.processors

import org.springframework.batch.item.ItemProcessor
import seko.es.join.service.domain.Item

class CompositeProcessor constructor(
    private val processors: List<ItemProcessor<Item, Item>>
) : ItemProcessor<Item, Item?> {
    override fun process(item: Item): Item? {
        for (processor in processors) {
            processor.process(item) ?: return null
        }
        return item
    }
}