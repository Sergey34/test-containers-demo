package seko.es.join.service.services

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import seko.es.join.service.domain.JobConfig
import seko.es.join.service.domain.Reader
import java.io.File

@Component
class JobConfigParser @Autowired constructor(private val objectMapper: ObjectMapper) : Parser<JobConfig>, Validator {
    companion object {
        @JvmField
        val ES_SCROLL_CONFIG_VALIDATOR = { config: Map<String, *> ->
            val scriptedFieldsConfigIsValid =
                    config["script_fields"] == null
                            || ((config["script_fields"] as List<Map<String, Any>>).all {
                        (it["field_name"] as String).isNotBlank() && (it["script"] as Map<String, String>).keys.containsAll(listOf("lang", "source"))
                    })
            config["query"] is String
                    && config["query"].toString().isNotBlank()
                    && (config["fields"] == null || (config["fields"] as List<String>).size > 0)
                    && (config["order"] == null || (config["order"] is Map<*, *> && (config["order"] as Map<*, *>).keys.containsAll(listOf("field", "type"))))
                    && scriptedFieldsConfigIsValid
        }
        @JvmField
        val VALIDATORS: Map<Reader.ReaderType, (Map<String, *>) -> Boolean> = mapOf(Reader.ReaderType.ES_SCROLL to ES_SCROLL_CONFIG_VALIDATOR)
    }

    override fun parse(jsonConfig: String): JobConfig {
        val jobConfig = objectMapper.readValue<JobConfig>(jsonConfig)
        jobConfig.steps.all {
            validate(it.reader.type, it.reader.config)
        }
        return jobConfig
    }

    override fun validate(type: Enum<*>, config: Map<String, *>): Boolean {
        return VALIDATORS[type]?.invoke(config) ?: false
    }

    override fun parse(file: File): JobConfig {
        return parse(file.readText())
    }

    override fun parseList(jsonConfig: String): List<JobConfig> {
        return objectMapper.readValue(jsonConfig)
    }

    override fun parseList(file: File): List<JobConfig> {
        return parseList(file.readText())
    }

    override fun validate(jsonConfig: String, type: Validator.Type): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun validate(file: File, type: Validator.Type): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

interface Parser<T> {
    fun parse(jsonConfig: String): T
    fun parse(file: File): T
    fun parseList(jsonConfig: String): List<T>
    fun parseList(file: File): List<T>
}

interface Validator {
    fun validate(type: Enum<*>, config: Map<String, *>): Boolean
    fun validate(jsonConfig: String, type: Type = Type.SINGLE): Boolean
    fun validate(file: File, type: Type = Type.SINGLE): Boolean
    enum class Type {
        SINGLE, COLLECTION
    }
}