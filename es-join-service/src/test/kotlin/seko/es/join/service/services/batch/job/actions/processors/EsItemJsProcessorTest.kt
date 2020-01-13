package seko.es.join.service.services.batch.job.actions.processors

import org.junit.Assert
import org.junit.jupiter.api.Test
import seko.es.join.service.domain.Item
import seko.es.join.service.domain.config.Configuration
import seko.es.join.service.domain.config.processors.ProcessorType

internal class EsItemJsProcessorTest {

    @Test
    fun process() {
        val processor = Configuration(ProcessorType.JS.toString(), mutableMapOf("script" to """
            function process(item) { 
              item.content.qweqweq = 12312312; 
              return item;
            }
        """.trimIndent()))
        val result = EsItemJsProcessor(processor).process(Item("", "", "", mutableMapOf("][poiu" to "34234")))
        Assert.assertEquals(Item("", "", "", mutableMapOf("][poiu" to "34234", "qweqweq" to 12312312)), result)
    }
}