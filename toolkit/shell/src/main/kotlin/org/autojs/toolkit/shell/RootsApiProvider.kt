package org.autojs.toolkit.shell

import org.autojs.core.api.ApiProvider
import org.autojs.core.api.ScriptScope
import org.autojs.core.runtime.callJsFunction
import org.mozilla.javascript.Function

/**
 * roots 命名空间：Root 能力（对齐 AutoJS Pro 的 roots 模块）。
 * 无 root 设备上所有调用安全降级（available=false，exec 返回错误码）。
 */
class RootsApiProvider : ApiProvider {
    override val name = "roots"
    private val executor = ShellExecutor()

    @Volatile
    private var cachedAvailable: Boolean? = null

    override fun install(scope: ScriptScope) {
        scope.defineObject("roots") {
            function("available") { _ ->
                cachedAvailable ?: executor.isRootAvailable().also { cachedAvailable = it }
            }
            function("exec") { args ->
                val cmd = args.getOrNull(0)?.toString() ?: ""
                executor.exec(cmd, root = true)
            }
            function("shell") { args ->
                val cmd = args.getOrNull(0)?.toString() ?: ""
                val cb = args.getOrNull(1) as? Function
                Thread {
                    val r = executor.exec(cmd, root = true)
                    if (cb != null) callJsFunction(cb, arrayOf(r))
                }.start()
                null
            }
        }
    }
}
