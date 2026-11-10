package org.autojs.toolkit.floaty

import android.view.View
import org.autojs.core.api.ApiProvider
import org.autojs.core.api.ScriptScope
import org.autojs.core.runtime.JsMapObject
import org.autojs.core.runtime.UiViewBinder
import org.mozilla.javascript.BaseFunction
import org.mozilla.javascript.Context
import org.mozilla.javascript.Scriptable

/**
 * floaty 命名空间 API：悬浮窗。window() 返回值带 id 动态绑定与 close()。
 */
class FloatyApiProvider(private val context: android.content.Context) : ApiProvider {
    override val name = "floaty"
    private val manager = FloatyManager(context)

    override fun install(scope: ScriptScope) {
        scope.defineObject("floaty") {
            function("window") { args ->
                val xml = args[0]?.toString() ?: return@function null
                val renderer = XmlViewRenderer(context)
                val idMap = HashMap<String, View>()
                val view = renderer.render(xml, null, idMap)
                val fw = manager.createWindow(view)
                val members = HashMap<String, Any?>()
                idMap.forEach { (id, v) -> members[id] = UiViewBinder(v) }
                members["raw"] = fw
                members["close"] = object : BaseFunction() {
                    override fun call(cx: Context, s: Scriptable, thisObj: Scriptable, a: Array<out Any?>): Any? {
                        manager.closeWindow(fw); return null
                    }
                }
                scope.createDynamicObject(members)
            }
            function("close") { args ->
                when (val w = args.getOrNull(0)) {
                    is FloatWindow -> manager.closeWindow(w)
                    is JsMapObject -> (w.member("raw") as? FloatWindow)?.let { manager.closeWindow(it) }
                }
                null
            }
            function("closeAll") { _ -> manager.closeAll(); null }
            function("isOverlaysEnabled") { _ -> manager.isOverlaysEnabled() }
            function("openOverlaySettings") { _ -> manager.openOverlaySettings() }
            function("console") { args ->
                val console = manager.createConsole()
                console.show()
                console
            }
            property("manager", manager)
        }
    }
}
