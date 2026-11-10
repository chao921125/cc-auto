package org.autojs.core.api

import android.view.KeyEvent
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

/**
 * 按键事件总线：无障碍服务 onKeyEvent 分发入口，events 命名空间注册监听。
 * 放在 core/api 以保持 toolkit 与 runtime 的依赖中立。
 */
object KeyEventBus {

    private val downListeners = ConcurrentHashMap<Int, CopyOnWriteArrayList<(Int) -> Unit>>()
    private val upListeners = ConcurrentHashMap<Int, CopyOnWriteArrayList<(Int) -> Unit>>()

    @Volatile
    var observing = false

    fun onDown(code: Int, fn: (Int) -> Unit) {
        downListeners.getOrPut(code) { CopyOnWriteArrayList() }.add(fn)
    }

    fun onUp(code: Int, fn: (Int) -> Unit) {
        upListeners.getOrPut(code) { CopyOnWriteArrayList() }.add(fn)
    }

    fun onceDown(code: Int, fn: (Int) -> Unit) {
        val list = downListeners.getOrPut(code) { CopyOnWriteArrayList() }
        lateinit var wrapper: (Int) -> Unit
        wrapper = { k -> fn(k); synchronized(list) { list.remove(wrapper) } }
        list.add(wrapper)
    }

    fun onceUp(code: Int, fn: (Int) -> Unit) {
        val list = upListeners.getOrPut(code) { CopyOnWriteArrayList() }
        lateinit var wrapper: (Int) -> Unit
        wrapper = { k -> fn(k); synchronized(list) { list.remove(wrapper) } }
        list.add(wrapper)
    }

    fun removeAll(code: Int) {
        downListeners.remove(code); upListeners.remove(code)
    }

    fun clearAll() {
        downListeners.clear(); upListeners.clear()
    }

    /** 由无障碍服务调用；返回是否有监听者（不消费事件，恒放行）。 */
    fun dispatch(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            downListeners[event.keyCode]?.forEach { runCatching { it(event.keyCode) } }
        } else if (event.action == KeyEvent.ACTION_UP) {
            upListeners[event.keyCode]?.forEach { runCatching { it(event.keyCode) } }
        }
        return false
    }
}
