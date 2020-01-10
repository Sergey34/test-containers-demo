package seko.es.join.service.services.batch.job.actions.processors

import org.junit.Assert
import org.junit.jupiter.api.Test
import seko.es.join.service.domain.Configuration
import seko.es.join.service.domain.processors.ProcessorType

internal class EsItemJsProcessorTest {

    @Test
    fun process() {
        val processor = Configuration(ProcessorType.JS.toString(), mutableMapOf("script" to """
            function process(source) { 
              source.qweqweq = 12312312; 
              return source;
            }
        """.trimIndent()))
        val result = EsItemJsProcessor(processor).process(mutableMapOf("][poiu" to "34234"))
        Assert.assertEquals(mapOf("][poiu" to "34234", "qweqweq" to 12312312), result)
    }
}