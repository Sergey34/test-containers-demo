package seko.es.join.service.configuration

import seko.es.join.service.services.constants.HistoryFields
import java.io.PrintWriter
import java.io.StringWriter

fun Throwable.getException(): Map<String, String?> {
    val sw = StringWriter()
    val pw = PrintWriter(sw)
    printStackTrace(pw)
    val stackTrace = sw.toString()
    return mapOf(HistoryFields.STACKTRACE to stackTrace, HistoryFields.MESSAGE to message)
}