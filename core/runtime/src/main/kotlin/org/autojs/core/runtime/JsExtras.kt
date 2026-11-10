package org.autojs.core.runtime

import org.mozilla.javascript.Context
import org.mozilla.javascript.Function
import org.mozilla.javascript.NativeObject
import org.mozilla.javascript.Scriptable

/** 在任意线程安全地调用 JS 函数；若当前线程无 Rhino Context 则临时进入/退出。 */
fun callJsFunction(fn: Function, args: Array<Any?> = emptyArray()) {
    val hadContext = Context.getCurrentContext() != null
    val cx = if (hadContext) Context.getCurrentContext()!! else Context.enter()
    try {
        val scope = fn.parentScope ?: cx.initStandardObjects()
        fn.call(cx, scope, scope, args)
    } catch (e: Exception) {
        // JS 回调异常不应击穿调用线程（如无障碍按键线程）
        android.util.Log.w("CcAuto", "JS 回调异常: ${e.message}")
    } finally {
        if (!hadContext) Context.exit()
    }
}

/**
 * 动态成员 JS 对象：用于 floaty.window 返回值等按 id 动态绑定的场景。
 */
class JsMapObject(private val members: MutableMap<String, Any?> = HashMap()) : NativeObject() {

    override fun get(name: String, start: Scriptable): Any? {
        if (members.containsKey(name)) {
            return Context.javaToJS(members[name], this)
        }
        return super.get(name, start)
    }

    override fun has(name: String, start: Scriptable): Boolean {
        return members.containsKey(name) || super.has(name, start)
    }

    override fun getIds(): Array<Any> {
        val result = ArrayList<Any>()
        result.addAll(super.getIds().toList())
        result.addAll(members.keys.toList<Any>())
        return result.toTypedArray()
    }

    fun member(name: String): Any? = members[name]
}
