package seko.es.join.service.services.config.parsing

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import seko.es.join.service.domain.JobConfig
import seko.es.join.service.domain.Reader
import seko.es.join.service.domain.Writer
import java.io.File

@Component
class JobConfigParser @Autowired constructor(private val objectMapper: ObjectMapper) : Parser<JobConfig>, Validator {
    override fun parse(jsonConfig: String): JobConfig {
        val jobConfig = objectMapper.readValue<JobConfig>(jsonConfig)
        val isValidConfig = jobConfig.steps.all {
            validate(it.reader.type, it.reader.config)
                    && validate(it.writer.type, it.writer.config)
                    && it.processor?.let { p -> validate(p.type, p.config) } ?: true
        }
        return if (isValidConfig) {
            jobConfig
        } else {
            throw IllegalStateException("Invalid config")
        }
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


    companion object {
        @JvmField
        val VALIDATE_SCRIPT = { config: Any? ->
            config == null || (config is List<*> && (config as List<Map<String, Any>>).all {
                (it["field_name"] as String).isNotBlank()
                        && (it["script"] as Map<String, String>).keys.containsAll(listOf("lang", "source"))
            })
        }


        @JvmField
        val ES_SCROLL_CONFIG_VALIDATOR = { config: Map<String, *> ->
            config["query"] is String
                    && config["query"].toString().isNotBlank()
                    && (config["fields"] == null || (config["fields"] as List<String>).size > 0)
                    && (config["order"] == null || (config["order"] is Map<*, *> && (config["order"] as Map<*, *>).keys.containsAll(listOf("field", "type"))))
                    && VALIDATE_SCRIPT(config["script_fields"])
        }
        @JvmField
        val ES_UPDATE_WRITER_CONFIG_VALIDATOR = { config: Map<String, *> ->
            config["field_with_doc_id"] is String
                    && config["field_with_doc_id"].toString().isNotBlank()
                    && (config["target_field"] == null || (config["target_field"].toString().isNotBlank()))
                    && (config["retry_on_conflict"] == null || config["retry_on_conflict"] is Int)
                    && (config["doc_as_upsert"] == null || config["doc_as_upsert"] is Boolean)
                    && VALIDATE_SCRIPT(config["script_fields"])
        }
        @JvmField
        val VALIDATORS: Map<Enum<*>, (Map<String, *>) -> Boolean> = mapOf(
                Reader.ReaderType.ES_SCROLL to ES_SCROLL_CONFIG_VALIDATOR,
                Writer.WriterType.UPDATE to ES_UPDATE_WRITER_CONFIG_VALIDATOR
        )
    }
}