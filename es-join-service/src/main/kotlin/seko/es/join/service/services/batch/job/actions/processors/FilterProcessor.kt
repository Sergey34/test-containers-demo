package seko.es.join.service.services.batch.job.actions.processors

import org.springframework.batch.item.ItemProcessor
import seko.es.join.service.domain.Configuration
import seko.es.join.service.domain.Item
import seko.es.join.service.domain.processors.ScriptProcessor
import seko.es.join.service.services.InvocableFactory
import javax.script.Invocable

class FilterProcessor(processor: Configuration) : ItemProcessor<Item, Item> {
    private val scriptProcessorConfig: ScriptProcessor = ScriptProcessor.from(processor.config)
    private val inv: Invocable

    init {
        inv = InvocableFactory.getInvocable(scriptProcessorConfig.script)
    }

    override fun process(item: Item): Item? {
        return if (inv.invokeFunction("filter", item) as Boolean) item else null
    }
}