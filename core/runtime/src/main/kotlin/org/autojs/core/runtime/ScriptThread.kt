package org.autojs.core.runtime

import org.autojs.core.api.*
import org.mozilla.javascript.*
import java.util.concurrent.CountDownLatch

/**
 * 脚本线程：每个脚本主线程和 threads.start() 创建的子线程各持有一个 Rhino Context。
 * 内含事件循环（timers + microtasks）。
 */
class ScriptThread(
    val runtime: ScriptRuntime,
    val source: ScriptSource,
    val isMain: Boolean = false
) : Thread("script-${source.name}") {

    @Volatile
    var returnValue: Any? = null

    /** 若设置，则本线程执行该 JS 函数（用于 threads.start），而非执行 source 代码。 */
    @Volatile
    var entryFunction: org.mozilla.javascript.Function? = null
    var entryArgs: Array<Any?> = emptyArray()

    private val finished = CountDownLatch(1)
    private val eventLoop = EventLoop(this)
    private var cx: Context? = null
    private var scope: ScriptableObject? = null

    val moduleLoader get() = runtime.moduleLoader
    val eventBus get() = runtime.eventBus

    init {
        setUncaughtExceptionHandler { th, ex ->
            runtime.log("E", "线程未捕获异常 ${th.name}: ${ex.message}")
        }
    }

    override fun run() {
        currentRuntime.set(runtime)
        try {
            cx = RhinoEngine.enterContext(runtime.config)
            val scope = cx!!.initStandardObjects()
            this.scope = scope

            // 注入 API 命名空间
            ApiRegistry.all().forEach { provider ->
                installProvider(cx!!, scope, provider)
            }

            // 注入内置全局函数
            installBuiltins(cx!!, scope)

            // 执行脚本
            val ef = entryFunction
            val result = if (ef != null) {
                ef.call(cx!!, scope, scope, entryArgs)
            } else {
                val code = source.getCode()
                cx!!.evaluateString(scope, code, source.name, 1, null)
            }
            returnValue = result

            // 运行事件循环直到所有 timer/子线程结束
            if (ef == null) eventLoop.runLoop()
        } catch (e: org.mozilla.javascript.RhinoException) {
            runtime.log("E", "${e.message} (line ${e.lineNumber()})")
            // 不再重抛：避免未捕获异常崩溃进程，错误已通过日志暴露
        } catch (e: InterruptedException) {
            interrupt()
        } catch (e: ScriptInterrupt) {
            runtime.log("I", "脚本已被中断: ${source.name}")
        } catch (e: Throwable) {
            runtime.log("E", "脚本线程异常: ${e.javaClass.simpleName}: ${e.message}")
        } finally {
            eventLoop.shutdown()
            Context.exit()
            currentRuntime.remove()
            runtime.removeThread(this)
            finished.countDown()
        }
    }

    fun startAndAwait() {
        start()
        finished.await()
    }

    fun awaitTimeout(timeoutMs: Long): Boolean {
        return finished.await(timeoutMs, java.util.concurrent.TimeUnit.MILLISECONDS)
    }

    private fun installProvider(cx: Context, scope: ScriptableObject, provider: ApiProvider) {
        try {
            // 直接把命名空间（defineObject）与顶层简写（defineFunction）注入全局作用域，
            // 避免 http.http.get / auto.auto.text 之类的双层嵌套。
            val installMethod = provider.javaClass.getMethod("install", ScriptScope::class.java)
            val scriptScope = RhinoScriptScope(cx, scope, scope)
            installMethod.invoke(provider, scriptScope)
        } catch (e: Exception) {
            runtime.log("W", "Failed to install API '${provider.name}': ${e.message}")
        }
    }

    private fun installBuiltins(cx: Context, scope: ScriptableObject) {
        val builtins = mapOf(
            "log" to { args: Array<out Any?> ->
                val msg = args.joinToString(" ") { it?.toString() ?: "undefined" }
                runtime.log("I", msg); null
            },
            "sleep" to { args: Array<out Any?> ->
                val ms = (args.getOrNull(0) as? Number)?.toLong() ?: 0L
                Thread.sleep(ms); null
            },
            "require" to { args: Array<out Any?> ->
                val moduleName = args.getOrNull(0) as? String ?: ""
                moduleLoader.require(cx, scope, moduleName)
            },
            "setTimeout" to { args: Array<out Any?> ->
                val fn = args.getOrNull(0)
                val delay = (args.getOrNull(1) as? Number)?.toLong() ?: 0L
                eventLoop.setTimeout(fn, delay)
            },
            "setInterval" to { args: Array<out Any?> ->
                val fn = args.getOrNull(0)
                val interval = (args.getOrNull(1) as? Number)?.toLong() ?: 0L
                eventLoop.setInterval(fn, interval)
            },
            "clearTimeout" to { args: Array<out Any?> ->
                val id = (args.getOrNull(0) as? Number)?.toInt() ?: -1
                eventLoop.clearTimer(id); null
            },
            "clearInterval" to { args: Array<out Any?> ->
                val id = (args.getOrNull(0) as? Number)?.toInt() ?: -1
                eventLoop.clearTimer(id); null
            },
            "exit" to { args: Array<out Any?> ->
                runtime.stop(); null
            },
            "toast" to { args: Array<out Any?> ->
                val msg = args.getOrNull(0)?.toString() ?: ""
                val ctx = AppRuntime.context
                if (ctx != null) {
                    val len = if ((args.getOrNull(1) as? Number)?.toInt() == 1)
                        android.widget.Toast.LENGTH_LONG else android.widget.Toast.LENGTH_SHORT
                    android.os.Handler(android.os.Looper.getMainLooper()).post {
                        runCatching { android.widget.Toast.makeText(ctx, msg, len).show() }
                    }
                }
                null
            },
            "random" to { args: Array<out Any?> ->
                val a = args.getOrNull(0) as? Number
                val b = args.getOrNull(1) as? Number
                if (a == null || b == null) Math.random()
                else (a.toInt() + Math.random() * (b.toInt() - a.toInt() + 1)).toInt().toDouble()
            }
        )
        builtins.forEach { (name, fn) ->
            val f = object : BaseFunction() {
                override fun call(cx: Context, scope: Scriptable, thisObj: Scriptable, args: Array<out Any?>): Any? {
                    return fn(args)
                }
            }
            ScriptableObject.defineProperty(scope, name, f, ScriptableObject.READONLY)
        }

        // 剪贴板（两种命名都给，对齐 AutoJS Pro 的 setClipBoard 拼写）
        val clipFns = mapOf(
            "setClipBoard" to { a: Array<out Any?> -> setClipBoard(a.getOrNull(0)?.toString() ?: ""); null },
            "setClipboard" to { a: Array<out Any?> -> setClipBoard(a.getOrNull(0)?.toString() ?: ""); null },
            "getClipBoard" to { _: Array<out Any?> -> getClipBoard() },
            "getClipboard" to { _: Array<out Any?> -> getClipBoard() }
        )
        clipFns.forEach { (name, fn) ->
            val f = object : BaseFunction() {
                override fun call(cx: Context, scope: Scriptable, thisObj: Scriptable, args: Array<out Any?>): Any? = fn(args)
            }
            ScriptableObject.defineProperty(scope, name, f, ScriptableObject.READONLY)
        }

        // 全局 shell：可调用且带 .exec/.isRootAvailable（覆盖 shell 命名空间对象，行为保持兼容）
        ScriptableObject.defineProperty(scope, "shell", ShellCallable(), ScriptableObject.READONLY)

        // console 作为对象：log/info/warn/error/verbose/debug/show/hide
        defineObject(scope, "console") {
            "log" to { a -> runtime.log("I", a.joinToString(" ")); null }
            "info" to { a -> runtime.log("I", a.joinToString(" ")); null }
            "debug" to { a -> runtime.log("D", a.joinToString(" ")); null }
            "warn" to { a -> runtime.log("W", a.joinToString(" ")); null }
            "error" to { a -> runtime.log("E", a.joinToString(" ")); null }
            "verbose" to { a -> runtime.log("V", a.joinToString(" ")); null }
            "show" to { _ -> true }
            "hide" to { _ -> null }
            "clear" to { _ -> null }
        }

        // util
        defineObject(scope, "util") {
            "format" to { a ->
                if (a.isEmpty()) "" else {
                    val fmt = a[0]?.toString() ?: ""
                    var i = 1
                    Regex("%([sdifj])").replace(fmt) { m ->
                        if (i >= a.size) m.value
                        else {
                            val v = a[i++]
                            when (m.groupValues[1]) {
                                "d", "i" -> ((v as? Number)?.toDouble()?.toLong() ?: 0L).toString()
                                "f" -> ((v as? Number)?.toDouble() ?: 0.0).toString()
                                else -> v?.toString() ?: "null"
                            }
                        }
                    }
                }
            }
            "uuid" to { _ -> java.util.UUID.randomUUID().toString() }
        }

        // base64
        defineObject(scope, "base64") {
            "encode" to { a ->
                val src = a.getOrNull(0)
                when (src) {
                    is String -> android.util.Base64.encodeToString(src.toByteArray(), android.util.Base64.NO_WRAP)
                    is ByteArray -> android.util.Base64.encodeToString(src, android.util.Base64.NO_WRAP)
                    else -> ""
                }
            }
            "decode" to { a ->
                val s = a.getOrNull(0)?.toString() ?: ""
                String(android.util.Base64.decode(s, android.util.Base64.DEFAULT))
            }
        }

        // crypto
        defineObject(scope, "crypto") {
            fun digest(algo: String, data: String): String {
                val bytes = java.security.MessageDigest.getInstance(algo).digest(data.toByteArray())
                return bytes.joinToString("") { b -> (b.toInt() and 0xFF).toString(16).padStart(2, '0') }
            }
            "md5" to { a -> digest("MD5", a.getOrNull(0)?.toString() ?: "") }
            "sha1" to { a -> digest("SHA-1", a.getOrNull(0)?.toString() ?: "") }
            "sha256" to { a -> digest("SHA-256", a.getOrNull(0)?.toString() ?: "") }
        }
    }

    /** 定义一个顶层对象，其成员为 name -> 函数。 */
    private fun defineObject(scope: ScriptableObject, name: String, members: ObjectMembers.() -> Unit) {
        val obj = cx?.newObject(scope) as? ScriptableObject ?: return
        val builder = ObjectMembers().apply(members)
        builder.entries.forEach { (key, fn) ->
            val f = object : BaseFunction() {
                override fun call(cx: Context, s: Scriptable, thisObj: Scriptable, args: Array<out Any?>): Any? = fn(args)
            }
            ScriptableObject.defineProperty(obj, key, f, ScriptableObject.EMPTY)
        }
        ScriptableObject.defineProperty(scope, name, obj, ScriptableObject.READONLY)
    }

    /** 收集对象成员：`"name" to { args -> ... }`。 */
    inner class ObjectMembers {
        val entries = linkedMapOf<String, (Array<out Any?>) -> Any?>()
        infix fun String.to(fn: (Array<out Any?>) -> Any?) { entries[this] = fn }
    }

    fun executeInContext(code: String, scriptName: String): Any? {
        val c = cx ?: return null
        val s = scope ?: return null
        return c.evaluateString(s, code, scriptName, 1, null)
    }

    companion object {
        /** 当前正在执行脚本的 runtime（供 threads/engines 内置 API 读取）。 */
        val currentRuntime = ThreadLocal<ScriptRuntime>()

        /** 当前脚本线程（供 threads.currentThread 读取）。 */
        fun current(): ScriptThread? = currentThread() as? ScriptThread
    }
}
