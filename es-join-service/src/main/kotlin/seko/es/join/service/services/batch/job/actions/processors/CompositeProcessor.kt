package seko.es.join.service.services.batch.job.actions.processors

import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.StepExecution
import org.springframework.batch.core.annotation.BeforeStep
import org.springframework.batch.item.ItemProcessor
import seko.es.join.service.domain.Item

class CompositeProcessor constructor(
    private val processors: List<ItemProcessor<Item, Item>>
) : ItemProcessor<Item, Item?> {
    private lateinit var jobExecution: JobExecution // add custom field to global job context

    override fun process(item: Item): Item? {
        for (processor in processors) {
            processor.process(item) ?: return null
        }
        return item
    }

    @BeforeStep
    fun beforeStep(stepExecution: StepExecution) {
        jobExecution = stepExecution.jobExecution
    }
}