package seko.es.join.service.repository

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.elasticsearch.client.RestHighLevelClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository
import seko.es.join.service.domain.JobConfig
import java.io.File

@Repository
class EsRepository @Autowired constructor(
    private val restHighLevelClient: RestHighLevelClient,
    private val objectMapper: ObjectMapper
) {

    fun getJobs(): List<JobConfig> {
        val jobs = objectMapper.readValue<List<JobConfig>>(File("t3.json").reader())
        return jobs
    }

}