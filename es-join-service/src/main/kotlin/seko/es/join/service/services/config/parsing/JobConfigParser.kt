package seko.es.join.service.services.config.parsing

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import seko.es.join.service.domain.JobConfig
import seko.es.join.service.domain.Processor
import seko.es.join.service.domain.Processor.JoinProcessor.Companion.JOIN_PROCESSOR_CONFIG_VALIDATOR
import seko.es.join.service.domain.Processor.MultiJoinProcessor.Companion.MULTI_JOIN_PROCESSOR_CONFIG_VALIDATOR
import seko.es.join.service.domain.Processor.ScriptProcessor.Companion.JS_PROCESSOR_CONFIG_VALIDATOR
import seko.es.join.service.domain.Reader
import seko.es.join.service.domain.Reader.EsScrollReader.Companion.ES_SCROLL_CONFIG_VALIDATOR
import seko.es.join.service.domain.Writer
import seko.es.join.service.domain.Writer.EsIndexWriter.Companion.ES_INDEX_WRITER_CONFIG_VALIDATOR
import seko.es.join.service.domain.Writer.EsUpdateWriter.Companion.ES_UPDATE_WRITER_CONFIG_VALIDATOR
import java.io.File

@Component
class JobConfigParser @Autowired constructor(
    private val objectMapper: ObjectMapper
) : Parser<JobConfig> {
    override fun parse(jsonConfig: String): JobConfig {
        val jobConfig = objectMapper.readValue<JobConfig>(jsonConfig)
        val isValidConfig = validateJobConfig(jobConfig)
        return if (isValidConfig) {
            jobConfig
        } else {
            throw IllegalStateException("Invalid config")
        }
    }

    fun validateJobConfig(jobConfig: JobConfig): Boolean {
        return jobConfig.steps.all {
            validate(it.reader.type, it.reader.config)
                    && validate(it.writer.type, it.writer.config)
                    && it.processors?.all { p -> validate(p.type, p.config) } ?: true
        }
    }

    fun validate(type: Enum<*>, config: Map<String, *>): Boolean {
        return VALIDATORS[type]?.invoke(config) ?: false
    }

    override fun parse(file: File): JobConfig {
        return parse(file.readText())
    }

    override fun parseList(jsonConfig: String): List<JobConfig> {
        return objectMapper.readValue(jsonConfig)
    }

    override fun parseList(file: File): List<JobConfig> {
        val jobConfigs = parseList(file.readText())
        val isValidConfigs = jobConfigs.all { validateJobConfig(it) }
        return if (isValidConfigs) {
            jobConfigs
        } else {
            throw IllegalStateException("Invalid config")
        }
    }

    companion object {
        @JvmField
        val VALIDATORS: Map<Enum<*>, (Map<String, *>) -> Boolean> = mapOf(
            Reader.ReaderType.ES_SCROLL to ES_SCROLL_CONFIG_VALIDATOR,
            Writer.WriterType.UPDATE to ES_UPDATE_WRITER_CONFIG_VALIDATOR,
            Processor.ProcessorType.JS to JS_PROCESSOR_CONFIG_VALIDATOR,
            Processor.ProcessorType.JOIN to JOIN_PROCESSOR_CONFIG_VALIDATOR,
            Writer.WriterType.INDEX to ES_INDEX_WRITER_CONFIG_VALIDATOR,
            Processor.ProcessorType.MULTI_JOIN to MULTI_JOIN_PROCESSOR_CONFIG_VALIDATOR
        )
    }
}