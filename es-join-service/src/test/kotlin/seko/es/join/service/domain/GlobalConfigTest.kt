package seko.es.join.service.domain

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.Assert
import org.junit.Test

class GlobalConfigTest {
    private val mapper: ObjectMapper = ObjectMapper().registerModule(KotlinModule())


    @Test
    fun testJsonFormat() {
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
    fun testJsonFormatWithoutRotation() {
        val config = mapper.readValue<GlobalConfig>(
            """{
      "target_index": "window"
    }"""
        )
        Assert.assertNotNull(config)
    }

    @Test
    fun testJsonFormatWithoutFormat() {
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