package seko.es.join.service.services.config.parsing

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import seko.es.join.service.domain.config.JobConfig
import seko.es.join.service.domain.config.processors.FilterProcessor.Companion.SCRIPT_PROCESSOR_CONFIG_VALIDATOR
import seko.es.join.service.domain.config.processors.JoinProcessor.Companion.JOIN_PROCESSOR_CONFIG_VALIDATOR
import seko.es.join.service.domain.config.processors.MultiJoinProcessor.Companion.MULTI_JOIN_PROCESSOR_CONFIG_VALIDATOR
import seko.es.join.service.domain.config.processors.ProcessorType
import seko.es.join.service.domain.config.processors.ScriptProcessor.Companion.JS_PROCESSOR_CONFIG_VALIDATOR
import seko.es.join.service.domain.config.readers.EsScrollReader.Companion.ES_SCROLL_CONFIG_VALIDATOR
import seko.es.join.service.domain.config.readers.IndicesReader.Companion.INDEX_CONFIG_VALIDATOR
import seko.es.join.service.domain.config.readers.ReaderType
import seko.es.join.service.domain.config.writers.DeleteDocument.Companion.ES_DELETE_DOCUMENTS_CONFIG_VALIDATOR
import seko.es.join.service.domain.config.writers.DeleteIndices.Companion.ES_DELETE_INDICES_CONFIG_VALIDATOR
import seko.es.join.service.domain.config.writers.EsIndexWriter.Companion.ES_INDEX_WRITER_CONFIG_VALIDATOR
import seko.es.join.service.domain.config.writers.EsUpdateWriter.Companion.ES_UPDATE_WRITER_CONFIG_VALIDATOR
import seko.es.join.service.domain.config.writers.WriterType
import seko.es.join.service.services.exceptions.InvalidConfigException
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
            throw InvalidConfigException()
        }
    }

    fun validateJobConfig(jobConfig: JobConfig): Boolean {
        return jobConfig.steps.all {
            validate(it.reader.type, it.reader.config)
                && validate(it.writer.type, it.writer.config)
                && it.processors?.all { p -> validate(p.type, p.config) } ?: true
        }
    }

    fun validate(type: String, config: Map<String, *>): Boolean {
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
            throw InvalidConfigException()
        }
    }

    companion object {
        @JvmField
        val VALIDATORS: Map<String, (Map<String, *>) -> Boolean> = mapOf(
            ReaderType.ES_SCROLL.toString() to ES_SCROLL_CONFIG_VALIDATOR,
            ReaderType.ES_INDICES.toString() to INDEX_CONFIG_VALIDATOR,
            WriterType.UPDATE.toString() to ES_UPDATE_WRITER_CONFIG_VALIDATOR,
            ProcessorType.JS.toString() to JS_PROCESSOR_CONFIG_VALIDATOR,
            ProcessorType.FILTER.toString() to SCRIPT_PROCESSOR_CONFIG_VALIDATOR,
            ProcessorType.JOIN.toString() to JOIN_PROCESSOR_CONFIG_VALIDATOR,
            WriterType.INDEX.toString() to ES_INDEX_WRITER_CONFIG_VALIDATOR,
            WriterType.INDEX_DELETE.toString() to ES_DELETE_INDICES_CONFIG_VALIDATOR,
            WriterType.DELETE_DOCUMENTS.toString() to ES_DELETE_DOCUMENTS_CONFIG_VALIDATOR,
            ProcessorType.MULTI_JOIN.toString() to MULTI_JOIN_PROCESSOR_CONFIG_VALIDATOR
        )
    }
}