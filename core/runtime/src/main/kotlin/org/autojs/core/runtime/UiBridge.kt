package org.autojs.core.runtime

import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.CompoundButton
import android.widget.ProgressBar
import android.widget.TextView
import org.mozilla.javascript.Context
import org.mozilla.javascript.Function
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

/**
 * UI 桥：core/runtime 定义契约，app 模块提供 Activity 宿主实现。
 */
interface UiHost {
    /** 展示脚本 UI，返回 id → View 绑定表；超时返回 null。 */
    fun show(xml: String, timeoutMs: Long = 5000): Map<String, View>?
    fun finish()
}

object UiBridge {
    @Volatile
    var host: UiHost? = null

    val mainHandler = Handler(Looper.getMainLooper())
}

/**
 * 脚本侧 ui.<id> 的视图包装：暴露常用方法供 Rhino 反射调用。
 */
class UiViewBinder(private val view: View) {

    fun on(event: String, fn: Function) {
        when (event) {
            "click" -> UiBridge.mainHandler.post { view.setOnClickListener { callJs(fn) } }
            "check", "checked_change" -> UiBridge.mainHandler.post {
                (view as? CompoundButton)?.setOnCheckedChangeListener { _, checked ->
                    callJs(fn, arrayOf(checked))
                }
            }
            "draw" -> UiBridge.mainHandler.post {
                (view as? org.autojs.core.runtime.canvas.ScriptCanvasView)?.let { cv ->
                    cv.drawCallback = { scriptCanvas -> callJs(fn, arrayOf(scriptCanvas)) }
                    cv.invalidate()
                }
            }
        }
    }

    fun click(fn: Function) = on("click", fn)

    /** 请求重绘（canvas 控件动画常用）。 */
    fun invalidate() = post { view.invalidate() }

    fun setText(value: String) = post { (view as? TextView)?.text = value }
    fun text(): String = (view as? TextView)?.text?.toString() ?: ""

    fun setHint(value: String) = post { (view as? TextView)?.hint = value }

    fun setProgress(value: Int) = post { (view as? ProgressBar)?.progress = value }
    fun getProgress(): Int = (view as? ProgressBar)?.progress ?: 0
    fun setMax(value: Int) = post { (view as? ProgressBar)?.max = value }

    fun setVisibility(value: Int) = post { view.visibility = value }
    fun getVisibility(): Int = view.visibility

    fun setChecked(value: Boolean) = post { (view as? CompoundButton)?.isChecked = value }

    /** 在主线程执行 JS 回调（与 AutoJS Pro 的 UI 事件语义一致）。 */
    private fun callJs(fn: Function, args: Array<Any?> = emptyArray()) {
        val cx = Context.enter()
        try {
            val scope = fn.parentScope ?: cx.initStandardObjects()
            fn.call(cx, scope, scope, args)
        } finally {
            Context.exit()
        }
    }

    private inline fun post(crossinline block: () -> Unit) {
        UiBridge.mainHandler.post { block() }
    }
}

/** 在主线程执行 JS 函数并阻塞等待结果（ui.run）。 */
fun runOnMainBlocking(fn: Function): Any? {
    val ref = AtomicReference<Any?>()
    val latch = CountDownLatch(1)
    UiBridge.mainHandler.post {
        val cx = Context.enter()
        try {
            val scope = fn.parentScope ?: cx.initStandardObjects()
            ref.set(fn.call(cx, scope, scope, emptyArray()))
        } finally {
            Context.exit()
            latch.countDown()
        }
    }
    latch.await(10, TimeUnit.SECONDS)
    return ref.get()
}
