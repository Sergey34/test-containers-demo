package seko.es.join.service.services.batch.job.actions.processors

import org.springframework.batch.item.ItemProcessor
import seko.es.join.service.domain.Configuration
import seko.es.join.service.domain.processors.ScriptProcessor
import seko.es.join.service.services.InvocableFactory
import javax.script.Invocable


class EsItemJsProcessor(processor: Configuration) : ItemProcessor<MutableMap<String, Any>, Map<String, Any>> {
    private val scriptProcessorConfig: ScriptProcessor = ScriptProcessor.from(processor.config)
    private val inv: Invocable

    init {
        inv = InvocableFactory.getInvocable(scriptProcessorConfig.script)
    }

    override fun process(item: MutableMap<String, Any>): Map<String, Any> {
        return inv.invokeFunction("process", item) as Map<String, Any>
    }
}