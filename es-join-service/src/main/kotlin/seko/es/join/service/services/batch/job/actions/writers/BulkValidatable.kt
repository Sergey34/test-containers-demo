package seko.es.join.service.services.batch.job.actions.writers

import org.elasticsearch.action.bulk.BulkResponse
import seko.es.join.service.configuration.getException
import seko.es.join.service.services.exceptions.BulkValidationException

interface BulkValidatable {
    fun validate(bulk: BulkResponse) {
        if (bulk.hasFailures()) {
            val failures = bulk.filter { it.isFailed }
                .map {
                    BulkValidation(it.index,
                        it.type,
                        it.id,
                        it.failureMessage,
                        it.opType.toString(),
                        it.failure.cause.getException()
                    )
                }
            throw BulkValidationException(failures)
        }
    }
}

