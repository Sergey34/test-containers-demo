package seko.es.join.service.configuration

import com.seko.testcontainers.es.EsTestContainer
import org.apache.http.HttpHost
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestHighLevelClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import javax.annotation.PreDestroy

@Configuration
@Profile("testcontainers")
class EsTestContainer {
    private val esTestContainer = EsTestContainer()
    @Bean
    @Primary
    fun esClient(): RestHighLevelClient {
        esTestContainer.start()
        return RestHighLevelClient(
                RestClient.builder(HttpHost("localhost", esTestContainer.port, "http")))
    }

    @PreDestroy
    fun preDestroy() {
        esTestContainer.close()
    }
}