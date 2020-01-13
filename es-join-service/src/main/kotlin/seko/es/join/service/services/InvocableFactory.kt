package seko.es.join.service.services

import jdk.nashorn.api.scripting.NashornScriptEngineFactory
import javax.script.Invocable

class InvocableFactory {
    companion object {
        @JvmField
        val ARGS = arrayOf("-strict", "--no-java", "--no-syntax-extensions")

        fun getInvocable(script: String): Invocable {
            val scriptEngine =
                NashornScriptEngineFactory().getScriptEngine(*ARGS)
            scriptEngine.eval(script)
            return scriptEngine as Invocable
        }
    }
}