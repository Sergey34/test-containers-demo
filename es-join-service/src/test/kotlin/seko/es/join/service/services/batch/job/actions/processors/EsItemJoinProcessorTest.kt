package seko.es.join.service.services.batch.job.actions.processors

import com.seko.testcontainers.es.EsTestContainer
import org.junit.ClassRule
import org.junit.jupiter.api.Test
import seko.es.join.service.domain.Configuration
import seko.es.join.service.domain.processors.ProcessorType

internal class EsItemJoinProcessorTest {
    @ClassRule
    val esTestContainer = EsTestContainer()

    @Test
    fun process() {
        val client = esTestContainer.client
        val processor = Configuration(ProcessorType.JOIN.toString(), mutableMapOf(
            "type" to "search",
            "index" to "order-*",
            "query" to """{"query": {"term": {"error_id.keyword": {"value": "{{id}}"}}}}""",
            "target_field" to "qwe",
            "params" to "id"
        ))
        val process = EsItemJoinProcessor(processor, client).process(mutableMapOf("id" to "1"))
    }
}