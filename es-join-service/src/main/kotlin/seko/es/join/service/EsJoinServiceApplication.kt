package seko.es.join.service

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.dataflow.server.EnableDataFlowServer

@SpringBootApplication
@EnableDataFlowServer
class EsJoinServiceApplication

fun main(args: Array<String>) {
    runApplication<EsJoinServiceApplication>(*args)
}
