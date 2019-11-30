package seko.es.join.service.services

import org.elasticsearch.client.RestHighLevelClient
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class JoinService @Autowired constructor(val restHighLevelClient: RestHighLevelClient) {
    @Autowired
    lateinit var jobLauncher: JobLauncher

    @Autowired
    lateinit var job: Job

    @Scheduled(cron = "0 * * * * ?")
    @Throws(Exception::class)
    fun perform() {
        val params = JobParametersBuilder()
            .addString("JobID", System.currentTimeMillis().toString())
            .toJobParameters()
        jobLauncher.run(job, params)
    }
}