package seko.es.join.service

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
@SpringBootApplication
class EsJoinServiceApplication

fun main(args: Array<String>) {
    runApplication<EsJoinServiceApplication>(*args)
}
