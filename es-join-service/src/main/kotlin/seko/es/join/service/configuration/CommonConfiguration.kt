package seko.es.join.service.configuration

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class CommonConfiguration {
    @Bean
    fun mapper(): ObjectMapper {
        return ObjectMapper().registerModule(KotlinModule())
    }
}