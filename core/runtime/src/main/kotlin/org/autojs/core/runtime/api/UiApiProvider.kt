package org.autojs.core.runtime.api

import org.autojs.core.api.ApiProvider
import org.autojs.core.api.ScriptScope
import org.autojs.core.runtime.UiBridge
import org.autojs.core.runtime.UiViewBinder
import org.autojs.core.runtime.runOnMainBlocking
import org.mozilla.javascript.Function

/**
 * ui 命名空间：脚本 UI（对齐 AutoJS Pro 的 ui.layout / id 绑定 / ui.run / ui.finish）。
 */
class UiApiProvider : ApiProvider {
    override val name = "ui"

    override fun install(scope: ScriptScope) {
        scope.defineObject("ui") {
            function("layout") { args ->
                val xml = args.getOrNull(0)?.toString() ?: return@function false
                val host = UiBridge.host ?: return@function false
                val views = host.show(xml) ?: return@function false
                views.forEach { (id, view) -> scope.bindMember("ui", id, UiViewBinder(view)) }
                true
            }
            function("finish") { _ -> UiBridge.host?.finish(); null }
            function("run") { args ->
                val fn = args.getOrNull(0) as? Function ?: return@function null
                runOnMainBlocking(fn)
            }
            function("isUiMode") { _ -> true }
        }
    }
}
