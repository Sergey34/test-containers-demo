package seko.es.join.service.services.batch.job.listeners

import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestHighLevelClient
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.JobExecutionListener
import org.springframework.batch.core.StepExecution
import org.springframework.batch.item.ExecutionContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class JobPersistStatisticExecutionListener @Autowired constructor(
    val restHighLevelClient: RestHighLevelClient
) : JobExecutionListener {
    override fun beforeJob(jobExecution: JobExecution) {
        jobExecution.executionContext.put("start", 123)
    }

    override fun afterJob(jobExecution: JobExecution) {
        jobExecution.executionContext.put("stepExecutions", jobExecution.stepExecutions.toEsDocs())
        val esDoc = jobExecution.executionContext.toMap()
        val source = IndexRequest(".join_history")
            .type("doc")
            .source(esDoc)
        restHighLevelClient.index(source, RequestOptions.DEFAULT)
    }

    private fun Collection<StepExecution>.toEsDocs(): List<Map<String, Any>> {
        return map {
            mapOf(
                "commit_count" to it.commitCount,
                "filter_count" to it.filterCount,
                "end_time" to it.endTime,
                "execution_context" to it.executionContext.toMap(),
                "exit_status" to mapOf(
                    "exit_code" to it.exitStatus.exitCode,
                    "exit_description" to it.exitStatus.exitDescription
                ),
                "failure_exceptions" to it.failureExceptions,
                "is_terminate_only" to it.isTerminateOnly,
                "job_execution_id" to it.jobExecutionId,
                "last_updated" to it.lastUpdated,
                "process_skip_count" to it.processSkipCount,
                "read_count" to it.readCount,
                "read_skip_count" to it.readSkipCount,
                "rollback_count" to it.rollbackCount,
                "step_name" to it.stepName,
                "status" to it.status,
                "summary" to it.summary,
                "write_count" to it.writeCount,
                "write_skip_count" to it.writeSkipCount
            )
        }
    }

    private fun ExecutionContext.toMap(): Map<String, Any> {
        return entrySet().map { it.key to it.value }.toMap()
    }
}
