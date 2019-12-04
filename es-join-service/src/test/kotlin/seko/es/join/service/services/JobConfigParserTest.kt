package seko.es.join.service.services

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.junit.Assert
import org.junit.jupiter.api.Test
import seko.es.join.service.services.config.parsing.JobConfigParser
import java.io.File

internal class JobConfigParserTest {
    private val jobConfigParser: JobConfigParser = JobConfigParser(ObjectMapper().registerModule(KotlinModule()))

    @Test
    fun parse() {
        val jobConfig = jobConfigParser.parse(File("t2.json"))
        Assert.assertNotNull(jobConfig)
    }
}