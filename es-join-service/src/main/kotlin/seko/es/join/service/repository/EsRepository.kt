package seko.es.join.service.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.elasticsearch.client.RestHighLevelClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository
import java.io.File
import java.lang.reflect.Type

@Repository
class EsRepository @Autowired constructor(private val restHighLevelClient: RestHighLevelClient) {
    fun getJobs(): List<Map<String, Any>> {
        val empMapType: Type = object : TypeToken<List<Map<String, Any>>>() {}.type
        return Gson().fromJson<List<Map<String, Any>>>(File("es-join-service/t.json").reader(), empMapType)
    }

}