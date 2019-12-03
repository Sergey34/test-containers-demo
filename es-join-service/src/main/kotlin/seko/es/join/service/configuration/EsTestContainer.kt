package seko.es.join.service.configuration

import com.seko.testcontainers.es.EsTestContainer
import org.apache.http.HttpHost
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestHighLevelClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.EventListener
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

@Configuration
@Profile("testcontainers")
class EsTestContainer {
    private val esTestContainer = EsTestContainer()
    @Bean
    @Primary
    fun esClient(): RestHighLevelClient {
        esTestContainer.start()
        val port = esTestContainer.port
        println(port)
        return RestHighLevelClient(
                RestClient.builder(HttpHost("localhost", port, "http")))
    }

    @PreDestroy
    fun preDestroy() {
        esTestContainer.close()
    }

    @EventListener(ContextRefreshedEvent::class)
    fun testDataInit() {
        esTestContainer.uploadData()
    }
}