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
import seko.es.join.service.configuration.getException
import seko.es.join.service.services.constants.EsConstants.Companion.DOC_TYPE
import seko.es.join.service.services.constants.EsConstants.Companion.HISTORY_INDEX
import seko.es.join.service.services.constants.HistoryFields.Companion.COMMIT_COUNT
import seko.es.join.service.services.constants.HistoryFields.Companion.END_TIME
import seko.es.join.service.services.constants.HistoryFields.Companion.EXECUTION_CONTEXT
import seko.es.join.service.services.constants.HistoryFields.Companion.EXIT_CODE
import seko.es.join.service.services.constants.HistoryFields.Companion.EXIT_DESCRIPTION
import seko.es.join.service.services.constants.HistoryFields.Companion.EXIT_STATUS
import seko.es.join.service.services.constants.HistoryFields.Companion.FAILURE_EXCEPTIONS
import seko.es.join.service.services.constants.HistoryFields.Companion.FILTER_COUNT
import seko.es.join.service.services.constants.HistoryFields.Companion.IS_TERMINATE_ONLY
import seko.es.join.service.services.constants.HistoryFields.Companion.JOB_EXECUTION_ID
import seko.es.join.service.services.constants.HistoryFields.Companion.LAST_UPDATED
import seko.es.join.service.services.constants.HistoryFields.Companion.PROCESS_SKIP_COUNT
import seko.es.join.service.services.constants.HistoryFields.Companion.READ_COUNT
import seko.es.join.service.services.constants.HistoryFields.Companion.READ_SKIP_COUNT
import seko.es.join.service.services.constants.HistoryFields.Companion.ROLLBACK_COUNT
import seko.es.join.service.services.constants.HistoryFields.Companion.START_TIME
import seko.es.join.service.services.constants.HistoryFields.Companion.STATUS
import seko.es.join.service.services.constants.HistoryFields.Companion.STEP_EXECUTIONS
import seko.es.join.service.services.constants.HistoryFields.Companion.STEP_NAME
import seko.es.join.service.services.constants.HistoryFields.Companion.SUMMARY
import seko.es.join.service.services.constants.HistoryFields.Companion.WRITE_COUNT
import seko.es.join.service.services.constants.HistoryFields.Companion.WRITE_SKIP_COUNT
import java.time.Clock
import java.time.LocalDate
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE

@Component
class JobPersistStatisticExecutionListener @Autowired constructor(
    private val restHighLevelClient: RestHighLevelClient
) : JobExecutionListener {
    override fun beforeJob(jobExecution: JobExecution) {
        // add custom info
    }

    override fun afterJob(jobExecution: JobExecution) {
        jobExecution.executionContext.put(STEP_EXECUTIONS, jobExecution.stepExecutions.toEsDocs())
        val esDoc = jobExecution.executionContext.toMap()
        val source = IndexRequest("$HISTORY_INDEX-${LocalDate.now(Clock.systemUTC()).format(ISO_LOCAL_DATE)}")
            .type(DOC_TYPE)
            .source(esDoc)
        restHighLevelClient.index(source, RequestOptions.DEFAULT)
    }

    private fun Collection<StepExecution>.toEsDocs(): List<Map<String, Any>> {
        return map {
            mapOf(
                COMMIT_COUNT to it.commitCount,
                FILTER_COUNT to it.filterCount,
                END_TIME to it.endTime,
                START_TIME to it.startTime,
                EXECUTION_CONTEXT to it.executionContext.toMap(),
                EXIT_STATUS to mapOf(
                    EXIT_CODE to it.exitStatus.exitCode,
                    EXIT_DESCRIPTION to it.exitStatus.exitDescription
                ),
                FAILURE_EXCEPTIONS to it.failureExceptions.map { e -> e.getException() },
                IS_TERMINATE_ONLY to it.isTerminateOnly,
                JOB_EXECUTION_ID to it.jobExecutionId,
                LAST_UPDATED to it.lastUpdated,
                PROCESS_SKIP_COUNT to it.processSkipCount,
                READ_COUNT to it.readCount,
                READ_SKIP_COUNT to it.readSkipCount,
                ROLLBACK_COUNT to it.rollbackCount,
                STEP_NAME to it.stepName,
                STATUS to it.status,
                SUMMARY to it.summary,
                WRITE_COUNT to it.writeCount,
                WRITE_SKIP_COUNT to it.writeSkipCount
            )
        }
    }

    private fun ExecutionContext.toMap(): Map<String, Any> {
        return entrySet().map { it.key to it.value }.toMap()
    }
}


