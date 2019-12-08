package seko.es.join.service.services.config.parsing

import java.io.File

interface Parser<T> {
    fun parse(jsonConfig: String): T
    fun parse(file: File): T
    fun parseList(jsonConfig: String): List<T>
    fun parseList(file: File): List<T>
}