package seko.es.join.service

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

/**
 * добавиь faultTolerant() и настроить политику пропусков
 * регистрация в зукипере (добавить либу)
 * подписка на znode в зукипере (добавит В либу)
 * реализовать вычисления слайса
 * */

@EnableScheduling
@SpringBootApplication
class EsJoinServiceApplication

fun main(args: Array<String>) {
    runApplication<EsJoinServiceApplication>(*args)
}
