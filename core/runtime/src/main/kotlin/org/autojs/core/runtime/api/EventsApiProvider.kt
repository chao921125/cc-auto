package org.autojs.core.runtime.api

import org.autojs.core.api.ApiProvider
import org.autojs.core.api.ScriptScope
import org.autojs.core.runtime.callJsFunction
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

            // ===== 硬件按键事件（经无障碍 filterKeyEvents）=====
            function("observeKey") { _ ->
                org.autojs.core.api.KeyEventBus.observing = true
                true
            }
            function("onKeyDown") { args ->
                val code = keyCode(args.getOrNull(0))
                val fn = args.getOrNull(1) as? Function ?: return@function null
                if (code != null) org.autojs.core.api.KeyEventBus.onDown(code) { k ->
                    callJsFunction(fn, arrayOf(k))
                }
                null
            }
            function("onKeyUp") { args ->
                val code = keyCode(args.getOrNull(0))
                val fn = args.getOrNull(1) as? Function ?: return@function null
                if (code != null) org.autojs.core.api.KeyEventBus.onUp(code) { k ->
                    callJsFunction(fn, arrayOf(k))
                }
                null
            }
            function("onceKeyDown") { args ->
                val code = keyCode(args.getOrNull(0))
                val fn = args.getOrNull(1) as? Function ?: return@function null
                if (code != null) org.autojs.core.api.KeyEventBus.onceDown(code) { k ->
                    callJsFunction(fn, arrayOf(k))
                }
                null
            }
            function("onceKeyUp") { args ->
                val code = keyCode(args.getOrNull(0))
                val fn = args.getOrNull(1) as? Function ?: return@function null
                if (code != null) org.autojs.core.api.KeyEventBus.onceUp(code) { k ->
                    callJsFunction(fn, arrayOf(k))
                }
                null
            }
            function("removeAllKeyListeners") { _ ->
                org.autojs.core.api.KeyEventBus.clearAll(); null
            }
        }
    }

    /** 键名或数字 → keyCode。 */
    private fun keyCode(v: Any?): Int? = when (v) {
        is Number -> v.toInt()
        is String -> when (v.lowercase()) {
            "back" -> android.view.KeyEvent.KEYCODE_BACK
            "home" -> android.view.KeyEvent.KEYCODE_HOME
            "menu" -> android.view.KeyEvent.KEYCODE_MENU
            "power" -> android.view.KeyEvent.KEYCODE_POWER
            "volume_up", "volup", "volumeup" -> android.view.KeyEvent.KEYCODE_VOLUME_UP
            "volume_down", "voldown", "volumedown" -> android.view.KeyEvent.KEYCODE_VOLUME_DOWN
            "volume_mute" -> android.view.KeyEvent.KEYCODE_VOLUME_MUTE
            "enter" -> android.view.KeyEvent.KEYCODE_ENTER
            "del", "delete" -> android.view.KeyEvent.KEYCODE_DEL
            "space" -> android.view.KeyEvent.KEYCODE_SPACE
            "tab" -> android.view.KeyEvent.KEYCODE_TAB
            "camera" -> android.view.KeyEvent.KEYCODE_CAMERA
            "app_switch" -> android.view.KeyEvent.KEYCODE_APP_SWITCH
            "dpad_up" -> android.view.KeyEvent.KEYCODE_DPAD_UP
            "dpad_down" -> android.view.KeyEvent.KEYCODE_DPAD_DOWN
            "dpad_left" -> android.view.KeyEvent.KEYCODE_DPAD_LEFT
            "dpad_right" -> android.view.KeyEvent.KEYCODE_DPAD_RIGHT
            "dpad_center" -> android.view.KeyEvent.KEYCODE_DPAD_CENTER
            else -> v.toIntOrNull()
        }
        else -> null
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
