package seko.es.join.service.domain


import com.fasterxml.jackson.annotation.JsonProperty

data class Writer(
    @JsonProperty("config")
    val config: Map<String, Any>,
    @JsonProperty("type")
    val type: WriterType


//        @JsonProperty("conflicts")
//        val conflicts: String?, // proceed
//        @JsonProperty("query")
//        val query: String?, // {"match_all": {}}
//        @JsonProperty("routing")
//        val routing: Int, // 1000
//
//        @JsonProperty("scroll_size")
//        val scrollSize: Int?, // 1000
//        @JsonProperty("slices")
//        val slices: Int?, // 5
) {
    class EsUpdateWriter(
        @JsonProperty("script")
        val script: Script?,
        @JsonProperty("doc_as_upsert")
        val docAsUpsert: Boolean = true,
        @JsonProperty("retry_on_conflict")
        val retryOnConflict: Int = 0,
        @JsonProperty("target_field")
        val targetField: String?,
        @JsonProperty("field_with_doc_id")
        val fieldWithDocId: String
    ) {
        companion object {
            fun from(config: Map<String, Any>): EsUpdateWriter {
                return EsUpdateWriter(
                    Script.from(config["script"] as Map<String, String>?),
                    config["doc_as_upsert"] as Boolean,
                    config["retry_on_conflict"] as Int,
                    config["target_field"] as String?,
                    config["field_with_doc_id"] as String
                )
            }
        }
    }

    enum class WriterType {
        UPDATE_BY_QUERY, UPDATE, UPDATE_BY_SCRIPT, INSERT
    }
}