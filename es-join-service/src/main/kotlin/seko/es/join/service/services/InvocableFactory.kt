package seko.es.join.service.services

import jdk.nashorn.api.scripting.NashornScriptEngineFactory
import javax.script.Invocable

class InvocableFactory {
    companion object {
        fun getInvocable(script: String): Invocable {
            val scriptEngine =
                NashornScriptEngineFactory().getScriptEngine("-strict", "--no-java", "--no-syntax-extensions")
            scriptEngine.eval(script)
            return scriptEngine as Invocable
        }
    }
}