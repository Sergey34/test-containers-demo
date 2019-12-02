package seko.es.join.service.domain


import com.fasterxml.jackson.annotation.JsonProperty

data class JobConfig(
    @JsonProperty("global_config")
    val globalConfig: GlobalConfig,
    @JsonProperty("job_description")
    val jobDescription: String,
    @JsonProperty("job_id")
    val jobId: String, // 1
    @JsonProperty("job_name")
    val jobName: String, // name
    @JsonProperty("schedule")
    val schedule: String, // 0 * * * * ?
    @JsonProperty("steps")
    val steps: List<StepConfig>
)