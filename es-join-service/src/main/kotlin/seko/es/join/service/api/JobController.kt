package seko.es.join.service.api

import org.elasticsearch.rest.RestStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import seko.es.join.service.domain.JobConfig
import seko.es.join.service.services.JobService

@RestController
class JobController @Autowired constructor(
    private val jobService: JobService
) {
    @GetMapping("/job/configs")
    fun getJobConfigs(): List<JobConfig> {
        return jobService.getJobConfigs()
    }

    @GetMapping("/job/CurrentlyExecutingJobs")
    fun getCurrentlyExecutingJobs(): List<Map<String, Any?>> {
        return jobService.getCurrentlyExecutingJobs()
    }

    @PostMapping("/job")
    fun addJob(jobConfig: JobConfig): RestStatus {
        return jobService.addJob(jobConfig)
    }

    @PostMapping("/job/{jobId}")
    fun runJob(jobId: String) {
        jobService.runJob(jobId)
    }
}