package seko.es.join.service.repository

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import org.elasticsearch.client.RestHighLevelClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository
import seko.es.join.service.domain.JobConfig
import java.io.File

@Repository
class EsRepository @Autowired constructor(private val restHighLevelClient: RestHighLevelClient) {
    private val mapper: ObjectMapper = ObjectMapper().registerModule(KotlinModule())

    fun getJobs(): List<JobConfig> {
        val jobs = mapper.readValue<List<JobConfig>>(File("es-join-service/t2.json").reader())
        return jobs
    }

}