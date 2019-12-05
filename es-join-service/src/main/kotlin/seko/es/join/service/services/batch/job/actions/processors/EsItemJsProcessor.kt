package seko.es.join.service.services.batch.job.actions.processors

import jdk.nashorn.api.scripting.NashornScriptEngineFactory
import org.springframework.batch.item.ItemProcessor
import seko.es.join.service.domain.Processor
import javax.script.Invocable


class EsItemJsProcessor(processor: Processor) : ItemProcessor<Map<String, Any>, Map<String, Any>> {
    private val scriptProcessorConfig: Processor.ScriptProcessor = Processor.ScriptProcessor.from(processor.config)
    private val inv: Invocable

    init {
        val scriptEngine = NashornScriptEngineFactory().getScriptEngine("-strict", "--no-java", "--no-syntax-extensions")
        scriptEngine.eval(scriptProcessorConfig.script)
        this.inv = scriptEngine as Invocable
    }

    override fun process(item: Map<String, Any>): Map<String, Any> {
        return inv.invokeFunction("process", item) as Map<String, Any>
    }
}