package seko.es.join.service.services.config.parsing

import java.io.File

interface Validator {
    fun validate(type: Enum<*>, config: Map<String, *>): Boolean
    fun validate(jsonConfig: String, type: Type = Type.SINGLE): Boolean
    fun validate(file: File, type: Type = Type.SINGLE): Boolean
    enum class Type {
        SINGLE, COLLECTION
    }
}