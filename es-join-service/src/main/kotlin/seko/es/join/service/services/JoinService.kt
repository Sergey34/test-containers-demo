package seko.es.join.service.services

import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.quartz.*
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobParameters
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
import seko.es.join.service.domain.JobConfig
import seko.es.join.service.repository.EsRepository
import seko.es.join.service.services.jobs.JoinJob

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
        jobDataMap["jobParams"] = createJobParams(jobConfig)
        jobDataMap["job"] = getJob(jobConfig)

        return JobBuilder.newJob(JoinJob::class.java)
            .withIdentity(jobConfig.jobId, "namespace")
            .withDescription(jobConfig.jobDescription)
            .usingJobData(jobDataMap)
            .storeDurably()
            .build()
    }

    private fun createJobParams(jobConfig: JobConfig): JobParameters {
        return JobParametersBuilder()
            .addString("JobID", jobConfig.jobId)
            .toJobParameters()
    }

    private fun getJob(jobConfig: JobConfig): Job {
        val jb = jobs[jobConfig.jobId]
            .incrementer(RunIdIncrementer())
        val steps: List<Step> = createSteps(jobConfig)
        val jobWithStep = jb.start(steps.first())
        if (steps.size > 1) {
            jobWithStep.apply {
                steps.subList(1, steps.size - 1).forEach { step ->
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
            val writer = createWriter(it)
            val chunkSize = it.chunkSize

            val sb = stepBuilderFactory[it.id]
                .chunk<Map<*, *>, Map<*, *>>(chunkSize)
                .reader(reader)

            processor?.let { p ->
                sb.processor(p)
            }

            sb.writer(writer).build()
        }
    }

    private fun createWriter(config: seko.es.join.service.domain.Step): EsItemWriter {
        val writerConfig = config.writer
        return EsItemWriter(restHighLevelClient)
    }

    private fun createProcessor(config: seko.es.join.service.domain.Step): ItemProcessor<Map<*, *>, Map<*, *>>? {
        val processorConfig = config.processor
        return null
    }

    private fun createReader(config: seko.es.join.service.domain.Step): ElasticsearchItemReader {
        val readerConfig = config.reader
        val searchRequest = SearchRequest("my_index")
        val searchSourceBuilder = SearchSourceBuilder()
        searchSourceBuilder.query(QueryBuilders.matchAllQuery())
        searchRequest.source(searchSourceBuilder)
        val elasticsearchItemReader = ElasticsearchItemReader(restHighLevelClient, searchRequest)
        elasticsearchItemReader.setName("test")
        return elasticsearchItemReader
    }
}