package seko.es.join.service.services.jobs

import org.quartz.JobExecutionContext
import org.quartz.Scheduler
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.quartz.QuartzJobBean
import org.springframework.stereotype.Component

@Component
abstract class AbstractJoinJob @Autowired constructor(
    private var scheduler: Scheduler
) : QuartzJobBean() {

    override fun executeInternal(context: JobExecutionContext) {
        scheduler.pauseJob(context.jobDetail.key)
        val params = context.mergedJobDataMap["jobParamsBuilder"] as JobParametersBuilder
        params.addString("RunTime", System.nanoTime().toString())
        context.mergedJobDataMap["jobParams"] = params.toJobParameters()

        try {
            action(context)
        } finally {
            scheduler.resumeJob(context.jobDetail.key)
        }
    }

    protected abstract fun action(context: JobExecutionContext)
}