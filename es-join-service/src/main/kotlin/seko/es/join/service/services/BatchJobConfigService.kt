package seko.es.join.service.services

import com.netcracker.platform.otrk.zookeeper.registrar.starter.service.StatusProviderService
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
import org.springframework.batch.item.ItemReader
import org.springframework.batch.item.ItemWriter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import seko.es.join.service.domain.Item
import seko.es.join.service.domain.config.GlobalConfig
import seko.es.join.service.domain.config.JobConfig
import seko.es.join.service.domain.config.StepConfig
import seko.es.join.service.domain.config.processors.ProcessorType
import seko.es.join.service.domain.config.readers.ReaderType
import seko.es.join.service.domain.config.writers.WriterType
import seko.es.join.service.repository.EsRepository
import seko.es.join.service.services.batch.job.actions.processors.*
import seko.es.join.service.services.batch.job.actions.readers.EsScrollItemReader
import seko.es.join.service.services.batch.job.actions.readers.IndicesReader
import seko.es.join.service.services.batch.job.actions.writers.DeleteDocumentWriter
import seko.es.join.service.services.batch.job.actions.writers.DeleteIndicesWriter
import seko.es.join.service.services.batch.job.actions.writers.EsItemIndexWriter
import seko.es.join.service.services.batch.job.actions.writers.EsItemUpdateWriter
import seko.es.join.service.services.batch.job.listeners.JobPersistStatisticExecutionListener
import seko.es.join.service.services.constants.JobConstant.Companion.JOB_ID

@Service
class BatchJobConfigService @Autowired constructor(
    private val stepBuilderFactory: StepBuilderFactory,
    private val restHighLevelClient: RestHighLevelClient,
    private val jobPersistStatisticExecutionListener: JobPersistStatisticExecutionListener,
    private val jobs: JobBuilderFactory,
    private val esRepository: EsRepository,
    private val statusProviderService: StatusProviderService
) {
    private final lateinit var configs: List<JobConfig>

    fun getJobConfigs(): List<JobConfig> {
        if (!::configs.isInitialized) {
            configs = esRepository.getJobs()
        }
        return configs
    }

    private fun getSliceConfig(): SliceBuilder {
        return statusProviderService.provideStatus()
            .let {
                SliceBuilder(it.payload.sliceId, it.payload.sliceMax)
            }
    }

    fun createJobParams(jobConfig: JobConfig): JobParametersBuilder {
        return JobParametersBuilder().addString(JOB_ID, jobConfig.jobId)
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
                .chunk<Item, Item>(it.chunkSize)
                .reader(createReader(it))
                .apply {
                    createProcessor(it)?.let { p -> processor(p) }
                    writer(createWriter(it, jobConfig.globalConfig))
                }.build()
        }
    }

    private fun createWriter(config: StepConfig, globalConfig: GlobalConfig): ItemWriter<Item> {
        val writerConfig = config.writer
        return when (WriterType.valueOf(writerConfig.type)) {
            WriterType.UPDATE -> EsItemUpdateWriter(restHighLevelClient, writerConfig, globalConfig)
            WriterType.INDEX -> EsItemIndexWriter(restHighLevelClient, writerConfig, globalConfig)
            WriterType.INDEX_DELETE -> DeleteIndicesWriter(restHighLevelClient)
            WriterType.DELETE_DOCUMENTS -> DeleteDocumentWriter(restHighLevelClient)
            WriterType.UPDATE_BY_QUERY -> TODO()
            WriterType.UPDATE_BY_SCRIPT -> TODO()
        }
    }

    private fun createProcessor(config: StepConfig): ItemProcessor<Item, Item?>? {
        return config.processors?.map {
            when (ProcessorType.valueOf(it.type)) {
                ProcessorType.JS -> EsItemJsProcessor(it)
                ProcessorType.FILTER -> FilterProcessor(it)
                ProcessorType.JOIN -> EsItemJoinProcessor(it, restHighLevelClient)
                ProcessorType.MULTI_JOIN -> EsMultiItemJoinProcessor(it, restHighLevelClient)
            }
        }?.let { CompositeProcessor(it) }
    }

    private fun createReader(config: StepConfig): ItemReader<Item> {
        val readerConfig = config.reader
        return when (ReaderType.valueOf(readerConfig.type)) {
            ReaderType.ES_SCROLL -> {
                EsScrollItemReader(restHighLevelClient, readerConfig, config.chunkSize)
                    .apply { setName(config.id) }
            }
            ReaderType.ES_INDICES -> {
                IndicesReader(restHighLevelClient, readerConfig)
                    .apply { setName(config.id) }
            }
        }
    }
}