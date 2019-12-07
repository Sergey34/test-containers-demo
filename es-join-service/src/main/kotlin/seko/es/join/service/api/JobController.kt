package seko.es.join.service.api

import org.elasticsearch.rest.RestStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import seko.es.join.service.domain.JobConfig
import seko.es.join.service.services.JoinService

@RestController
class JobController @Autowired constructor(
    private val joinService: JoinService
) {
    @GetMapping("/job/configs")
    fun getJobConfigs(): List<JobConfig> {
        return joinService.getJobConfigs()
    }

    @GetMapping("/job/CurrentlyExecutingJobs")
    fun getCurrentlyExecutingJobs(): List<Map<String, Any?>> {
        return joinService.getCurrentlyExecutingJobs()
    }

    @PostMapping("/job")
    fun addJob(jobConfig: JobConfig): RestStatus {
        return joinService.addJob(jobConfig)
    }

    @PostMapping("/job/{jobId}")
    fun runJob(jobId: String) {
        joinService.runJob(jobId)
    }
}