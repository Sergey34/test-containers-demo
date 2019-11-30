package seko.es.join.service.repository

import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.JobInstance
import org.springframework.batch.core.JobParameters
import org.springframework.batch.core.StepExecution
import org.springframework.batch.core.repository.JobRepository

class EsJobRepository : JobRepository {
    override fun addAll(stepExecutions: MutableCollection<StepExecution>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun update(jobExecution: JobExecution) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun update(stepExecution: StepExecution) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun add(stepExecution: StepExecution) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun createJobInstance(jobName: String, jobParameters: JobParameters): JobInstance {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun createJobExecution(
        jobInstance: JobInstance,
        jobParameters: JobParameters,
        jobConfigurationLocation: String
    ): JobExecution {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun createJobExecution(jobName: String, jobParameters: JobParameters): JobExecution {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun updateExecutionContext(stepExecution: StepExecution) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun updateExecutionContext(jobExecution: JobExecution) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getStepExecutionCount(jobInstance: JobInstance, stepName: String): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getLastJobExecution(jobName: String, jobParameters: JobParameters): JobExecution? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getLastStepExecution(jobInstance: JobInstance, stepName: String): StepExecution? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun isJobInstanceExists(jobName: String, jobParameters: JobParameters): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}