package seko.es.join.service.services.batch.job.actions

import org.springframework.batch.item.ItemProcessor
import seko.es.join.service.domain.Processor

class EsItemProcessor (processor: Processor): ItemProcessor<Map<String, Any>, Map<String, Any>> {
    override fun process(item: Map<String, Any>): Map<String, Any> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}