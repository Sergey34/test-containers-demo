package seko.es.join.service.domain


import com.fasterxml.jackson.annotation.JsonProperty

data class ScriptField(
        @JsonProperty("field_name")
        val fieldName: String, // field_1
        @JsonProperty("script")
        val script: Script
) {
    companion object {
        fun from(config: Map<String, Any>): ScriptField {
            return ScriptField(config["field_name"] as String, Script.from(config["script"] as Map<String, String>))
        }
    }
}