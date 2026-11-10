package org.autojs.app.engine

import android.content.Context
import org.autojs.core.api.ApiRegistry
import org.autojs.core.api.RuntimeConfig
import org.autojs.core.api.ScriptSource
import org.autojs.core.runtime.ScriptRuntime
import org.autojs.toolkit.accessibility.AutoApiProvider
import org.autojs.toolkit.device.AppApiProvider
import org.autojs.toolkit.device.DeviceApiProvider
import org.autojs.toolkit.floaty.FloatyApiProvider
import org.autojs.toolkit.storage.FilesApiProvider
import org.autojs.toolkit.storage.StoragesApiProvider
import org.autojs.toolkit.network.HttpApiProvider
import org.autojs.toolkit.shell.ShellApiProvider
import org.autojs.toolkit.vision.ImagesApiProvider
import org.autojs.toolkit.vision.OcrApiProvider
import java.util.concurrent.ConcurrentHashMap

/**
 * 引擎管理器：全局单例，负责注册所有 API、管理脚本运行时。
 */
class EngineManager(private val context: Context) {

    private val runtimes = ConcurrentHashMap<String, ScriptRuntime>()

    init {
        org.autojs.core.runtime.AppRuntime.context = context
        registerApis()
    }

    private fun registerApis() {
        val scriptDir = context.filesDir.absolutePath + "/scripts"
        ApiRegistry.apply {
            register(AutoApiProvider())
            register(ImagesApiProvider())
            register(OcrApiProvider())
            register(HttpApiProvider())
            register(FilesApiProvider(scriptDir))
            register(StoragesApiProvider())
            register(DeviceApiProvider(context))
            register(AppApiProvider(context))
            register(ShellApiProvider())
            register(org.autojs.toolkit.shell.RootsApiProvider())
            register(FloatyApiProvider(context))
            register(org.autojs.core.runtime.api.ThreadsApiProvider())
            register(org.autojs.core.runtime.api.EnginesApiProvider())
            register(org.autojs.core.runtime.api.EventsApiProvider())
            register(org.autojs.core.runtime.api.DialogsApiProvider())
            register(org.autojs.core.runtime.api.UiApiProvider())
        }
    }

    /**
     * 执行脚本文件。
     */
    fun executeScript(path: String, config: RuntimeConfig = RuntimeConfig()): String {
        val source = ScriptSource.FileSource(
            name = java.io.File(path).name,
            path = path
        )
        return executeSource(source, config)
    }

    /**
     * 执行脚本代码。
     */
    fun executeCode(code: String, name: String = "inline", config: RuntimeConfig = RuntimeConfig()): String {
        val source = ScriptSource.MemorySource(name, code)
        return executeSource(source, config)
    }

    private fun executeSource(source: ScriptSource, config: RuntimeConfig): String {
        val runtime = ScriptRuntime(source, config.copy(scriptDir = context.filesDir.absolutePath + "/scripts"))
        runtimes[runtime.id] = runtime
        val t = Thread {
            try {
                runtime.run()
            } catch (e: Throwable) {
                // 兜底：脚本主线程任何未捕获异常都不应崩溃整个 App，转为错误日志
                runtime.log("E", "脚本异常终止: ${source.name} - ${e.message}")
            } finally {
                runtimes.remove(runtime.id)
            }
        }
        t.name = "engine-${source.name}"
        t.setUncaughtExceptionHandler { th, ex ->
            runtime.log("E", "引擎线程未捕获异常: ${th.name} - ${ex.message}")
        }
        t.start()
        return runtime.id
    }

    fun stopScript(id: String): Boolean {
        runtimes[id]?.stop()
        return runtimes.remove(id) != null
    }

    fun stopAll() {
        runtimes.values.forEach { it.stop() }
        runtimes.clear()
    }

    fun getRuntime(id: String): ScriptRuntime? = runtimes[id]

    fun getAllRuntimes(): Collection<ScriptRuntime> = runtimes.values

    fun isRunning(): Boolean = runtimes.isNotEmpty()

    companion object {
        @Volatile
        private var instance: EngineManager? = null

        fun get(context: Context): EngineManager {
            return instance ?: synchronized(this) {
                instance ?: EngineManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
