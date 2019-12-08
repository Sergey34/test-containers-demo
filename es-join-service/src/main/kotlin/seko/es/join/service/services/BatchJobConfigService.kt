package seko.es.join.service.services

import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.search.slice.SliceBuilder
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.core.step.tasklet.TaskletStep
import org.springframework.batch.item.ItemProcessor
import org.springframework.batch.item.ItemWriter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import seko.es.join.service.domain.*
import seko.es.join.service.repository.EsRepository
import seko.es.join.service.services.batch.job.actions.processors.CompositeProcessor
import seko.es.join.service.services.batch.job.actions.processors.EsItemJoinProcessor
import seko.es.join.service.services.batch.job.actions.processors.EsItemJsProcessor
import seko.es.join.service.services.batch.job.actions.processors.EsMultiItemJoinProcessor
import seko.es.join.service.services.batch.job.actions.readers.EsScrollItemReader
import seko.es.join.service.services.batch.job.actions.writers.EsItemIndexWriter
import seko.es.join.service.services.batch.job.actions.writers.EsItemUpdateWriter
import seko.es.join.service.services.batch.job.listeners.JobPersistStatisticExecutionListener

@Service
class BatchJobConfigService @Autowired constructor(
    private val stepBuilderFactory: StepBuilderFactory,
    private val restHighLevelClient: RestHighLevelClient,
    private val jobPersistStatisticExecutionListener: JobPersistStatisticExecutionListener,
    private val jobs: JobBuilderFactory,
    private val esRepository: EsRepository
) {
    private final lateinit var configs: List<JobConfig>

    fun getJobConfigs(): List<JobConfig> {
        if (!::configs.isInitialized) {
            configs = esRepository.getJobs()
        }
        return configs
    }

    private fun getSliceConfig(): SliceBuilder {
        return SliceBuilder(0, 1)
    }

    fun createJobParams(jobConfig: JobConfig): JobParametersBuilder {
        return JobParametersBuilder().addString("JobID", jobConfig.jobId)
    }

    fun getJob(jobConfig: JobConfig): Job {
        val jb = jobs[jobConfig.jobId].incrementer(RunIdIncrementer())
        val steps: List<Step> = createSteps(jobConfig)
        val jobWithStep = jb.start(steps.first())
        if (steps.size > 1) {
            jobWithStep.apply {
                steps.subList(1, steps.size).forEach { next(it) }
            }
        }
        jobWithStep.listener(jobPersistStatisticExecutionListener)
        return jobWithStep.build()
    }

    private fun createSteps(jobConfig: JobConfig): List<TaskletStep> {
        return jobConfig.steps.map {
            stepBuilderFactory[it.id]
                .chunk<MutableMap<String, Any>, Map<String, Any>>(it.chunkSize)
                .reader(createReader(it))
                .apply {
                    createProcessor(it)?.let { p -> processor(p) }
                    writer(createWriter(it, jobConfig.globalConfig))
                }.build()
        }
    }

    private fun createWriter(config: StepConfig, globalConfig: GlobalConfig): ItemWriter<Map<String, Any>> {
        val writerConfig = config.writer
        return when (writerConfig.type) {
            Writer.WriterType.UPDATE -> EsItemUpdateWriter(restHighLevelClient, writerConfig, globalConfig)
            Writer.WriterType.INDEX -> EsItemIndexWriter(restHighLevelClient, writerConfig, globalConfig)
            Writer.WriterType.UPDATE_BY_QUERY -> TODO()
            Writer.WriterType.UPDATE_BY_SCRIPT -> TODO()
        }
    }

    private fun createProcessor(config: StepConfig): ItemProcessor<MutableMap<String, Any>, Map<String, Any>>? {
        return config.processors?.map {
            when (it.type) {
                Processor.ProcessorType.JS -> EsItemJsProcessor(it)
                Processor.ProcessorType.JOIN -> EsItemJoinProcessor(it, restHighLevelClient)
                Processor.ProcessorType.MULTI_JOIN -> EsMultiItemJoinProcessor(it, restHighLevelClient)
            }
        }?.let { CompositeProcessor(it) }
    }

    private fun createReader(config: StepConfig): EsScrollItemReader {
        val readerConfig = config.reader
        return when (readerConfig.type) {
            Reader.ReaderType.ES_SCROLL -> {
                EsScrollItemReader(restHighLevelClient, readerConfig, config.chunkSize)
                    .apply { setName(config.id) }
            }
        }
    }
}