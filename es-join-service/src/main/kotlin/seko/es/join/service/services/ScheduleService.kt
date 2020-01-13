package seko.es.join.service.services

import org.quartz.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import seko.es.join.service.domain.config.JobConfig
import seko.es.join.service.services.constants.JobConstant.Companion.CONFIG
import seko.es.join.service.services.constants.JobConstant.Companion.JOB_PARAMS
import seko.es.join.service.services.constants.JobConstant.Companion.JOB_PARAMS_BUILDER

@Service
class ScheduleService @Autowired constructor(
    private val scheduler: Scheduler,
    @Value("spring.application.namespace") private val namespace: String
) {
    fun scheduleJob(buildJobDetail: JobDetail, it: JobConfig) {
        scheduler.scheduleJob(buildJobDetail, buildTrigger(it))
    }

    private fun buildTrigger(jobConfig: JobConfig): CronTrigger {
        return TriggerBuilder
            .newTrigger()
            .withIdentity(jobConfig.jobId, namespace)
            .withSchedule(CronScheduleBuilder.cronSchedule(jobConfig.schedule))
            .build()
    }

    fun getJobDetail(jobKey: JobKey): JobDetail? {
        if (scheduler.checkExists(jobKey)) {
            return scheduler.getJobDetail(jobKey)
        }
        return null
    }

    fun scheduleRunOnceJob(jobDetail: JobDetail) {
        val runOnceTrigger = TriggerBuilder.newTrigger().build()
        scheduler.scheduleJob(jobDetail, runOnceTrigger)
    }

    fun getCurrentlyExecutingJobs(): List<Map<String, Any?>> {
        return scheduler.currentlyExecutingJobs.map {
            mapOf(
                JOB_PARAMS to it.mergedJobDataMap[JOB_PARAMS],
                JOB_PARAMS_BUILDER to it.mergedJobDataMap[JOB_PARAMS_BUILDER],
                CONFIG to it.mergedJobDataMap[CONFIG]
            )
        }
    }
}