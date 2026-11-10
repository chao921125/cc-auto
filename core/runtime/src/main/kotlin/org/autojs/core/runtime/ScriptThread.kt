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

    private val finished = CountDownLatch(1)
    private val eventLoop = EventLoop(this)
    private var cx: Context? = null
    private var scope: ScriptableObject? = null

    val moduleLoader get() = runtime.moduleLoader
    val eventBus get() = runtime.eventBus

    override fun run() {
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
            val code = source.getCode()
            val result = cx!!.evaluateString(scope, code, source.name, 1, null)
            returnValue = result

            // 运行事件循环直到所有 timer/子线程结束
            eventLoop.runLoop()
        } catch (e: org.mozilla.javascript.RhinoException) {
            runtime.log("E", "${e.message} (line ${e.lineNumber()})")
            if (isMain) throw ScriptException("RHINO_ERROR", e.message ?: "JS error", e.lineNumber(), cause = e)
        } catch (e: InterruptedException) {
            interrupt()
        } catch (e: ScriptInterrupt) {
            if (isMain) throw e
        } finally {
            eventLoop.shutdown()
            Context.exit()
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
            val jsObj = cx.newObject(scope) as ScriptableObject
            val builder = ScriptObjectBuilder()
            // 通过反射调用 provider 的 install 方法
            // 这里简化处理：直接创建 JS 对象并注入
            val installMethod = provider.javaClass.getMethod("install", ScriptScope::class.java)
            val scriptScope = RhinoScriptScope(cx, scope, jsObj)
            installMethod.invoke(provider, scriptScope)
            ScriptableObject.defineProperty(scope, provider.name, jsObj, ScriptableObject.READONLY)
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
}
