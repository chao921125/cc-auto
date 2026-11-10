package org.autojs.core.runtime

import java.util.concurrent.ConcurrentHashMap

/**
 * 运行时登记表：记录当前所有活跃的 ScriptRuntime，供 engines 命名空间查询/停止。
 */
object RuntimeRegistry {
    private val runtimes = ConcurrentHashMap<String, ScriptRuntime>()

    internal fun add(rt: ScriptRuntime) { runtimes[rt.id] = rt }
    internal fun remove(rt: ScriptRuntime) { runtimes.remove(rt.id) }

    fun all(): Collection<ScriptRuntime> = runtimes.values.toList()
    fun get(id: String): ScriptRuntime? = runtimes[id]
    fun count(): Int = runtimes.size

    fun stopAll() { runtimes.values.forEach { it.stop() } }
}
