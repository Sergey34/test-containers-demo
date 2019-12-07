package seko.es.join.service.services

import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.search.slice.SliceBuilder
import org.quartz.*
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
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import seko.es.join.service.domain.*
import seko.es.join.service.domain.Processor.ProcessorType.JOIN
import seko.es.join.service.domain.Processor.ProcessorType.JS
import seko.es.join.service.repository.EsRepository
import seko.es.join.service.services.batch.job.actions.processors.CompositeProcessor
import seko.es.join.service.services.batch.job.actions.processors.EsItemJoinProcessor
import seko.es.join.service.services.batch.job.actions.processors.EsItemJsProcessor
import seko.es.join.service.services.batch.job.actions.readers.EsScrollItemReader
import seko.es.join.service.services.batch.job.actions.writers.EsItemIndexWriter
import seko.es.join.service.services.batch.job.actions.writers.EsItemUpdateWriter
import seko.es.join.service.services.quartz.jobs.JoinJob


@Service
class JoinService @Autowired constructor(
    val esRepository: EsRepository,
    val scheduler: Scheduler,
    val stepBuilderFactory: StepBuilderFactory,
    val restHighLevelClient: RestHighLevelClient,
    var jobs: JobBuilderFactory
) {
    @EventListener(ContextRefreshedEvent::class)
    fun initScheduling() {
        esRepository.getJobs()
            .forEach {
                scheduler.scheduleJob(buildJobDetail(it), buildTrigger(it))
            }
    }

    private fun buildTrigger(jobConfig: JobConfig): CronTrigger {
        return TriggerBuilder
            .newTrigger()
            .withIdentity(jobConfig.jobId, "namespace")
            .withSchedule(CronScheduleBuilder.cronSchedule(jobConfig.schedule))
            .build()
    }

    private fun buildJobDetail(jobConfig: JobConfig): JobDetail {
        val jobDataMap = JobDataMap()
        jobDataMap["config"] = jobConfig
        jobDataMap["jobParamsBuilder"] = createJobParams(jobConfig)
        jobDataMap["job"] = getJob(jobConfig)

        return JobBuilder.newJob(JoinJob::class.java)
            .withIdentity(jobConfig.jobId, "namespace")
            .withDescription(jobConfig.jobDescription)
            .usingJobData(jobDataMap)
            .storeDurably()
            .build()
    }

    private fun createJobParams(jobConfig: JobConfig): JobParametersBuilder {
        return JobParametersBuilder()
            .addString("JobID", jobConfig.jobId)
    }

    private fun getJob(jobConfig: JobConfig): Job {
        val jb = jobs[jobConfig.jobId]
            .incrementer(RunIdIncrementer())
        val steps: List<Step> = createSteps(jobConfig)
        val jobWithStep = jb.start(steps.first())
        if (steps.size > 1) {
            jobWithStep.apply {
                steps.subList(1, steps.size).forEach { step ->
                    next(step)
                }
            }
        }
        return jobWithStep.build()
    }

    private fun createSteps(jobConfig: JobConfig): List<TaskletStep> {
        val stepsConfig = jobConfig.steps
        return stepsConfig.map {
            val reader = createReader(it)
            val processor = createProcessor(it)
            val writer = createWriter(it, jobConfig.globalConfig)
            val chunkSize = it.chunkSize

            val sb = stepBuilderFactory[it.id]
                .chunk<MutableMap<String, Any>, Map<String, Any>>(chunkSize)
                .reader(reader)

            sb.apply {
                processor?.let { p -> processor(p) }
                writer(writer)
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
                JS -> EsItemJsProcessor(it)
                JOIN -> EsItemJoinProcessor(it, restHighLevelClient)
            }
        }?.let {
            CompositeProcessor(it)
        }
    }

    private fun createReader(config: StepConfig): EsScrollItemReader {
        val readerConfig = config.reader
        when (readerConfig.type) {
            Reader.ReaderType.ES_SCROLL -> {
                val elasticsearchItemReader = EsScrollItemReader(restHighLevelClient, readerConfig, config.chunkSize)
                elasticsearchItemReader.setName(config.id)
                return elasticsearchItemReader
            }
        }
    }

    fun getJobConfigs(): List<JobConfig> {
        return esRepository.getConfig(getSliceConfig())
    }

    private fun getSliceConfig(): SliceBuilder {
        return SliceBuilder(0, 1)
    }

    fun getCurrentlyExecutingJobs(): List<Map<String, Any?>> {
        return scheduler.currentlyExecutingJobs.map {
            mapOf(
                "jobParams" to it.mergedJobDataMap["jobParams"],
                "jobParamsBuilder" to it.mergedJobDataMap["jobParamsBuilder"],
                "config" to it.mergedJobDataMap["config"]
            )
        }
    }

    fun addJob(jobConfig: JobConfig): JobConfig {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}