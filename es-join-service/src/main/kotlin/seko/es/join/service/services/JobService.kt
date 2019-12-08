package seko.es.join.service.services

import org.elasticsearch.rest.RestStatus
import org.quartz.JobBuilder
import org.quartz.JobDataMap
import org.quartz.JobDetail
import org.quartz.JobKey
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import seko.es.join.service.domain.JobConfig
import seko.es.join.service.repository.EsRepository
import seko.es.join.service.services.config.parsing.JobConfigParser
import seko.es.join.service.services.quartz.jobs.JoinJob


@Service
class JobService @Autowired constructor(
    private val scheduleService: ScheduleService,
    private val batchJobConfigService: BatchJobConfigService,
    private val esRepository: EsRepository,
    private val parser: JobConfigParser
) {
    @EventListener(ContextRefreshedEvent::class)
    fun initScheduling() {
        batchJobConfigService.getJobConfigs().forEach {
            scheduleService.scheduleJob(buildJobDetail(it), it)
        }
    }

    private fun buildJobDetail(jobConfig: JobConfig): JobDetail {
        val jobDataMap = JobDataMap()
        jobDataMap["config"] = jobConfig
        jobDataMap["jobParamsBuilder"] = batchJobConfigService.createJobParams(jobConfig)
        jobDataMap["job"] = batchJobConfigService.getJob(jobConfig)

        return JobBuilder.newJob(JoinJob::class.java)
            .withIdentity(jobConfig.jobId, "namespace")
            .withDescription(jobConfig.jobDescription)
            .usingJobData(jobDataMap)
            .storeDurably()
            .build()
    }

    fun addJob(jobConfig: JobConfig): RestStatus {
        if (parser.validateJobConfig(jobConfig)) {
            return esRepository.save(jobConfig)
        } else {
            throw IllegalArgumentException("Invalid config")
        }
    }

    fun runJob(jobId: String) {
        val jobKey = JobKey.jobKey(jobId, "namespace")
        val jobDetail = scheduleService.getJobDetail(jobKey) ?: buildJobDetail(esRepository.getJob(jobId))
        scheduleService.scheduleRunOnceJob(jobDetail)
    }
}