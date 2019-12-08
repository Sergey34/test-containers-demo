package seko.es.join.service.api

import org.elasticsearch.rest.RestStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import seko.es.join.service.domain.JobConfig
import seko.es.join.service.services.BatchJobConfigService
import seko.es.join.service.services.JobService
import seko.es.join.service.services.ScheduleService

@RestController
class JobController @Autowired constructor(
    private val jobService: JobService,
    private val batchJobConfigService: BatchJobConfigService,
    private val scheduleService: ScheduleService
) {
    @GetMapping("/job/configs")
    fun getJobConfigs(): List<JobConfig> {
        return batchJobConfigService.getJobConfigs()
    }

    @GetMapping("/job/CurrentlyExecutingJobs")
    fun getCurrentlyExecutingJobs(): List<Map<String, Any?>> {
        return scheduleService.getCurrentlyExecutingJobs()
    }

    @PostMapping("/job")
    fun addJob(jobConfig: JobConfig): RestStatus {
        return jobService.addJob(jobConfig)
    }

    @PostMapping("/job/{jobId}")
    fun runJob(@PathVariable jobId: String) {
        jobService.runJob(jobId)
    }
}