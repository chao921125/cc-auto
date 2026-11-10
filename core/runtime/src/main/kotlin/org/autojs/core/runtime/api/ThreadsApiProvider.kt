package org.autojs.core.runtime.api

import org.autojs.core.api.ApiProvider
import org.autojs.core.api.ScriptSource
import org.autojs.core.api.ScriptScope
import org.autojs.core.runtime.ScriptThread
import org.mozilla.javascript.Function

/**
 * threads 命名空间：脚本内多线程。对齐 AutoJS Pro 的 threads.start / shutDownAll / currentThread。
 */
class ThreadsApiProvider : ApiProvider {
    override val name = "threads"

    override fun install(scope: ScriptScope) {
        scope.defineObject("threads") {
            function("start") { args ->
                val runtime = ScriptThread.currentRuntime.get() ?: return@function null
                val fn = args.getOrNull(0) as? Function ?: return@function null
                val callArgs = args.drop(1).toTypedArray()
                val t = ScriptThread(runtime, ScriptSource.MemorySource("thread", ""), isMain = false)
                t.entryFunction = fn
                t.entryArgs = callArgs
                runtime.addThread(t)
                t.start()
                // 返回 Thread 对象，JS 可调 t.interrupt()/t.join()/t.isAlive()
                t as Any?
            }
            function("shutDownAll") { _ ->
                ScriptThread.currentRuntime.get()?.shutDownThreads()
                null
            }
            function("currentThread") { _ -> ScriptThread.current() as Any? }
            function("currentThreadName") { _ -> Thread.currentThread().name }
        }
    }
}
