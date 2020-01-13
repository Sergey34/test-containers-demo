package seko.es.join.service.services.quartz.jobs

import org.quartz.JobExecutionContext
import org.quartz.Scheduler
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.quartz.QuartzJobBean
import org.springframework.stereotype.Component
import seko.es.join.service.services.constants.JobConstant.Companion.JOB_PARAMS
import seko.es.join.service.services.constants.JobConstant.Companion.JOB_PARAMS_BUILDER
import seko.es.join.service.services.constants.JobConstant.Companion.RUN_TIME

@Component
abstract class AbstractJoinJob @Autowired constructor(
    private var scheduler: Scheduler
) : QuartzJobBean() {

    override fun executeInternal(context: JobExecutionContext) {
        scheduler.pauseJob(context.jobDetail.key)
        val params = context.mergedJobDataMap[JOB_PARAMS_BUILDER] as JobParametersBuilder
        params.addString(RUN_TIME, System.nanoTime().toString())
        context.mergedJobDataMap[JOB_PARAMS] = params.toJobParameters()

        try {
            action(context)
        } finally {
            scheduler.resumeJob(context.jobDetail.key)
        }
    }

    protected abstract fun action(context: JobExecutionContext)
}