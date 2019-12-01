package seko.es.join.service.domain

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.Assert
import org.junit.Test

class GlobalConfigTest {
    private val mapper: ObjectMapper = ObjectMapper().registerModule(KotlinModule()) //let Jackson know about Kotlin


    @Test
    fun testJsonFormat(): Unit {
        val config = mapper.readValue<GlobalConfig>(
            """{
      "target_index": "window",
      "rotation_target_index_type": "DAILY",
      "rotation_target_index_date_format": "YYYY-MM"
    }"""
        )
        Assert.assertNotNull(config)
    }

    @Test
    fun testJsonFormatWithoutRotation(): Unit {
        val config = mapper.readValue<GlobalConfig>(
            """{
      "target_index": "window"
    }"""
        )
        Assert.assertNotNull(config)
    }

    @Test
    fun testJsonFormatWithoutFormat(): Unit {
        val config = mapper.readValue<GlobalConfig>(
            """
    {
      "target_index": "window",
      "rotation_target_index_type": "DAILY"
    }
    """
        )
        Assert.assertNotNull(config)
    }
}