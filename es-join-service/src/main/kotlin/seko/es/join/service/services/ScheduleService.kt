package seko.es.join.service.services

import org.quartz.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import seko.es.join.service.domain.JobConfig

@Service
class ScheduleService @Autowired constructor(
    private val scheduler: Scheduler
) {
    fun scheduleJob(buildJobDetail: JobDetail, it: JobConfig) {
        scheduler.scheduleJob(buildJobDetail, buildTrigger(it))
    }

    private fun buildTrigger(jobConfig: JobConfig): CronTrigger {
        return TriggerBuilder
            .newTrigger()
            .withIdentity(jobConfig.jobId, "namespace")
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
                "jobParams" to it.mergedJobDataMap["jobParams"],
                "jobParamsBuilder" to it.mergedJobDataMap["jobParamsBuilder"],
                "config" to it.mergedJobDataMap["config"]
            )
        }
    }
}