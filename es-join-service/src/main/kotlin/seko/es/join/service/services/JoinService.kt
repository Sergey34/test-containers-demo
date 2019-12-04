package seko.es.join.service.services

import org.elasticsearch.client.RestHighLevelClient
import org.quartz.*
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.core.step.tasklet.TaskletStep
import org.springframework.batch.item.ItemProcessor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import seko.es.join.service.domain.GlobalConfig
import seko.es.join.service.domain.JobConfig
import seko.es.join.service.repository.EsRepository
import seko.es.join.service.services.batch.job.actions.EsItemReader
import seko.es.join.service.services.batch.job.actions.EsItemWriter
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
            .withSchedule(
                CronScheduleBuilder.cronSchedule(jobConfig.schedule)
            )
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
                .chunk<Map<String, Any>, Map<String, Any>>(chunkSize)
                .reader(reader)

            processor?.let { p ->
                sb.processor(p)
            }

            sb.writer(writer).build()
        }
    }

    private fun createWriter(config: seko.es.join.service.domain.StepConfig, globalConfig: GlobalConfig): EsItemWriter {
        val writerConfig = config.writer
        return EsItemWriter(restHighLevelClient, writerConfig, globalConfig)
    }

    private fun createProcessor(config: seko.es.join.service.domain.StepConfig): ItemProcessor<Map<String, Any>, Map<String, Any>>? {
        val processorConfig = config.processor
        return null
    }

    private fun createReader(config: seko.es.join.service.domain.StepConfig): EsItemReader {
        val readerConfig = config.reader
        val elasticsearchItemReader = EsItemReader(restHighLevelClient, readerConfig, config.chunkSize)
        elasticsearchItemReader.setName(config.id)
        return elasticsearchItemReader
    }
}