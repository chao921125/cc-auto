package org.autojs.core.runtime.api

import android.app.AlertDialog
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.widget.EditText
import org.autojs.core.api.ApiProvider
import org.autojs.core.api.ScriptScope
import org.autojs.core.runtime.AppRuntime
import org.mozilla.javascript.Function
import org.mozilla.javascript.NativeArray

/**
 * dialogs 命名空间：脚本中弹出对话框。阻塞式返回结果，或传入回调。
 * 所有 UI 操作投递到主线程执行，脚本线程等待结果。
 */
class DialogsApiProvider : ApiProvider {
    override val name = "dialogs"
    private val main = Handler(Looper.getMainLooper())

    override fun install(scope: ScriptScope) {
        scope.defineObject("dialogs") {
            function("alert") { args ->
                val (title, content, cb) = parseTitleContent(args)
                showBlocking(cb) { ctx, done ->
                    AlertDialog.Builder(ctx).setTitle(title).setMessage(content)
                        .setPositiveButton("确定") { d, _ -> d.dismiss(); done(Unit) }
                        .setOnCancelListener { done(Unit) }
                        .create()
                }
            }
            function("confirm") { args ->
                val (title, content, cb) = parseTitleContent(args)
                val result = showBlocking<Boolean>(cb) { ctx, done ->
                    AlertDialog.Builder(ctx).setTitle(title).setMessage(content)
                        .setPositiveButton("确定") { d, _ -> d.dismiss(); done(true) }
                        .setNegativeButton("取消") { d, _ -> d.dismiss(); done(false) }
                        .setOnCancelListener { done(false) }
                        .create()
                }
                result
            }
            function("rawInput") { args ->
                val (title, hint, cb) = parseTitleContent(args)
                val ctx = AppRuntime.context ?: return@function null
                val result = showBlocking<String?>(cb) { c, done ->
                    val input = EditText(c).apply {
                        this.hint = hint; inputType = InputType.TYPE_CLASS_TEXT
                    }
                    AlertDialog.Builder(c).setTitle(title).setView(input)
                        .setPositiveButton("确定") { d, _ -> done(input.text.toString()); d.dismiss() }
                        .setNegativeButton("取消") { d, _ -> done(null); d.dismiss() }
                        .setOnCancelListener { done(null) }
                        .create()
                }
                result
            }
            function("singleChoice") { args ->
                val title = args.getOrNull(0)?.toString() ?: "请选择"
                val items = toStringArray(args.getOrNull(1))
                val cb = args.getOrNull(2) as? Function
                val ctx = AppRuntime.context ?: return@function -1
                val result = showBlocking<Int>(if (cb != null) cb else null) { c, done ->
                    AlertDialog.Builder(c).setTitle(title)
                        .setItems(items) { d, which -> done(which); d.dismiss() }
                        .setOnCancelListener { done(-1) }
                        .create()
                }
                result
            }
        }
    }

    private fun title2(s: String?) = s ?: ""

    private fun parseTitleContent(args: Array<out Any?>): Triple<String, String, Function?> {
        // 兼容 alert(content) 与 alert(title, content[, callback])
        return when {
            args.size >= 2 && args[1] is Function -> Triple(args[0]?.toString() ?: "", "", args[1] as Function)
            args.size >= 2 && args.size >= 3 && args[2] is Function -> Triple(args[0]?.toString() ?: "", args[1]?.toString() ?: "", args[2] as Function)
            args.size >= 2 -> Triple(args[0]?.toString() ?: "", args[1]?.toString() ?: "", null)
            else -> Triple("", args.getOrNull(0)?.toString() ?: "", null)
        }
    }

    private fun toStringArray(v: Any?): Array<CharSequence> = when (v) {
        is NativeArray -> v.toArray().map { it?.toString() ?: "" }.toTypedArray()
        is Array<*> -> v.map { it?.toString() ?: "" }.toTypedArray()
        else -> emptyArray()
    }

    /** 在主线程显示对话框；若提供回调则异步，否则阻塞脚本线程等待结果。 */
    private fun <T> showBlocking(callback: Function?, build: (android.content.Context, (T) -> Unit) -> AlertDialog): T? {
        val ctx = AppRuntime.context ?: return null
        if (callback == null) {
            val latch = java.util.concurrent.CountDownLatch(1)
            val ref = java.util.concurrent.atomic.AtomicReference<T?>()
            main.post {
                val dlg = build(ctx) { r -> ref.set(r); latch.countDown() }
                dlg.show()
            }
            try {
                latch.await()
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
            }
            return ref.get()
        } else {
            main.post {
                val dlg = build(ctx) { r ->
                    val cx = org.mozilla.javascript.Context.getCurrentContext() ?: org.mozilla.javascript.Context.enter()
                    val s = cx.initStandardObjects()
                    runCatching { callback.call(cx, s, s, arrayOf<Any?>(r)) }
                }
                dlg.show()
            }
            return null
        }
    }
}
