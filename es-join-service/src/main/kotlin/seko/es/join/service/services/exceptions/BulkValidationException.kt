package seko.es.join.service.services.exceptions

import seko.es.join.service.services.batch.job.actions.writers.BulkValidation

class BulkValidationException(failures: List<BulkValidation>) : RuntimeException("Bulk has failures")