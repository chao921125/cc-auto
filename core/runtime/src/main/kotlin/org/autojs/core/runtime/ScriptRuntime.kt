package org.autojs.core.runtime

import org.autojs.core.api.*
import org.mozilla.javascript.*
import org.mozilla.javascript.ContextFactory
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 脚本运行时：封装 Rhino 引擎 + 线程模型 + 事件循环 + 沙箱。
 * 每个脚本实例对应一个 ScriptRuntime。
 */
class ScriptRuntime(
    val source: ScriptSource,
    val config: RuntimeConfig = RuntimeConfig()
) {
    val id: String = "rt-${System.nanoTime()}"
    private val alive = AtomicBoolean(true)
    private val threads = mutableListOf<ScriptThread>()
    private var mainThread: ScriptThread? = null
    private var startTime: Long = 0

    val eventBus = EventBus()
    val moduleLoader = ModuleLoader(config.scriptDir)
    val storages = mutableMapOf<String, Any>()

    @Volatile
    var onLog: ((level: String, msg: String) -> Unit)? = null

    @Volatile
    var onEvent: ((ScriptEvent) -> Unit)? = null

    fun run(): ExecutionResult {
        startTime = System.currentTimeMillis()
        onEvent?.invoke(ScriptEvent.START)
        try {
            val thread = ScriptThread(this, source, isMain = true)
            mainThread = thread
            synchronized(threads) { threads.add(thread) }
            thread.startAndAwait()
            val duration = System.currentTimeMillis() - startTime
            onEvent?.invoke(ScriptEvent.EXIT)
            return ExecutionResult(true, thread.returnValue, durationMs = duration)
        } catch (e: ScriptInterrupt) {
            onEvent?.invoke(ScriptEvent.INTERRUPT)
            val duration = System.currentTimeMillis() - startTime
            return ExecutionResult(false, durationMs = duration)
        } catch (e: Throwable) {
            onEvent?.invoke(ScriptEvent.CRASH)
            val duration = System.currentTimeMillis() - startTime
            val scriptEx = ScriptException(
                "RUNTIME_ERROR",
                e.message ?: "Unknown error",
                stack = e.stackTraceToString(),
                cause = e
            )
            return ExecutionResult(false, error = scriptEx, durationMs = duration)
        } finally {
            cleanup()
        }
    }

    fun stop() {
        if (!alive.compareAndSet(true, false)) return
        synchronized(threads) {
            threads.forEach { it.interrupt() }
        }
    }

    fun isAlive(): Boolean = alive.get()

    /** 运行起始时间戳（未开始时为 0）。 */
    fun startedAt(): Long = startTime

    fun addThread(thread: ScriptThread) {
        synchronized(threads) { threads.add(thread) }
    }

    fun removeThread(thread: ScriptThread) {
        synchronized(threads) { threads.remove(thread) }
        if (thread.isMain && threads.all { !it.isAlive }) {
            stop()
        }
    }

    fun log(level: String, msg: String) {
        // 同时输出到 logcat（便于调试与 adb logcat 取证）
        when (level) {
            "E" -> android.util.Log.e("CcAuto", msg)
            "W" -> android.util.Log.w("CcAuto", msg)
            else -> android.util.Log.i("CcAuto", msg)
        }
        onLog?.invoke(level, msg)
    }

    private fun cleanup() {
        alive.set(false)
        synchronized(threads) {
            threads.forEach { it.interrupt() }
            threads.clear()
        }
        eventBus.clear()
    }
}
