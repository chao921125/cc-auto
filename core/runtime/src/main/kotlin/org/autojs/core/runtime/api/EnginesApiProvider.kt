package org.autojs.core.runtime.api

import org.autojs.core.api.ApiProvider
import org.autojs.core.api.ScriptScope
import org.autojs.core.runtime.RuntimeRegistry
import org.autojs.core.runtime.ScriptRuntime
import org.autojs.core.runtime.ScriptThread

/**
 * engines 命名空间：查询/控制运行中的脚本引擎。
 */
class EnginesApiProvider : ApiProvider {
    override val name = "engines"

    override fun install(scope: ScriptScope) {
        scope.defineObject("engines") {
            function("myEngine") { _ -> ScriptThread.currentRuntime.get() as Any? }
            function("myScript") { _ -> ScriptThread.currentRuntime.get()?.source?.name }
            function("myId") { _ -> ScriptThread.currentRuntime.get()?.id }
            function("stopAllScripts") { _ -> RuntimeRegistry.stopAll(); null }
            function("runningScriptCount") { _ -> RuntimeRegistry.count() }
            function("all") { _ -> RuntimeRegistry.all().map { it.source.name } }
        }
    }
}
