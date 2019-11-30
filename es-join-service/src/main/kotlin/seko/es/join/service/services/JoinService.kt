package seko.es.join.service.services

import org.elasticsearch.client.RestHighLevelClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct

@Service
class JoinService @Autowired constructor(val restHighLevelClient: RestHighLevelClient) {
    @PostConstruct
    fun init(): Unit {
        println()
    }
}