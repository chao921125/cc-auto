package org.autojs.core.runtime.api

import org.autojs.core.api.ApiProvider
import org.autojs.core.api.ScriptScope
import org.mozilla.javascript.Context
import org.mozilla.javascript.Function
import org.mozilla.javascript.Scriptable
import java.util.concurrent.CopyOnWriteArrayList

/**
 * events 命名空间：提供事件发射器 events.emitter()。
 * 说明：键盘/触摸等系统级事件监听需要 root 或无障碍注入，属后续增强；此处提供通用 Emitter。
 */
class EventsApiProvider : ApiProvider {
    override val name = "events"

    private val global = Emitter(null)

    override fun install(scope: ScriptScope) {
        scope.defineObject("events") {
            function("emitter") { _ ->
                val target = scope.get("events") as? Scriptable
                Emitter(target)
            }
            // 全局事件总线：events.on / once / off / emit
            function("on") { args ->
                global.on(args[0]?.toString() ?: "", args[1] as Function); null
            }
            function("once") { args ->
                global.once(args[0]?.toString() ?: "", args[1] as Function); null
            }
            function("removeAllListeners") { args ->
                global.off(args[0]?.toString() ?: ""); null
            }
            function("emit") { args ->
                val event = args[0]?.toString() ?: ""
                global.emit(event, *args.drop(1).toTypedArray())
                null
            }
        }
    }

    /** 通用事件发射器，暴露给 JS 调用 on/once/off/emit。 */
    class Emitter(private var scope: Scriptable?) {
        private val listeners = HashMap<String, CopyOnWriteArrayList<Function>>()
        private val onceListeners = HashMap<String, CopyOnWriteArrayList<Function>>()

        fun on(event: String, fn: Function) {
            listeners.getOrPut(event) { CopyOnWriteArrayList() }.add(fn)
        }

        fun once(event: String, fn: Function) {
            onceListeners.getOrPut(event) { CopyOnWriteArrayList() }.add(fn)
        }

        fun off(event: String) {
            listeners.remove(event); onceListeners.remove(event)
        }

        fun emit(event: String, vararg args: Any?) {
            val cx = Context.getCurrentContext() ?: Context.enter().also { Context.exit() }
            val s = scope ?: cx.initStandardObjects().also { scope = it }
            val a = args
            listeners[event]?.forEach { runCatching { it.call(cx, s, s, a) } }
            val once = onceListeners[event]
            if (once != null && !once.isEmpty()) {
                once.forEach { runCatching { it.call(cx, s, s, a) } }
                once.clear()
            }
        }
    }
}
