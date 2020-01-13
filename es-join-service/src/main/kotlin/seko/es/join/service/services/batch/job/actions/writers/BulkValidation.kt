package seko.es.join.service.services.batch.job.actions.writers

data class BulkValidation(
    val index: String?,
    val type: String?,
    val id: String?,
    val failureMessage: String?,
    val opType: String,
    val exception: Map<String, String?>
)