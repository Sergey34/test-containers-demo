package seko.es.join.service.services.quartz.jobs

import org.quartz.JobExecutionContext
import org.quartz.Scheduler
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobParameters
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class JoinJob @Autowired constructor(
    scheduler: Scheduler,
    private val jobLauncher: JobLauncher
) : AbstractJoinJob(scheduler) {
    override fun action(context: JobExecutionContext) {
        val job = context.mergedJobDataMap["job"] as Job
        val params = context.mergedJobDataMap["jobParams"] as JobParameters
        jobLauncher.run(job, params)
    }
}