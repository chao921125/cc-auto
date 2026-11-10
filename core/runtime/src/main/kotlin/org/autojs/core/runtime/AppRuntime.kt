package org.autojs.core.runtime

import android.content.Context

/**
 * 全局运行时上下文持有者：供内置函数（如 toast）访问 Application Context。
 * 由 EngineManager 在初始化时注入。
 */
object AppRuntime {
    @Volatile
    var context: Context? = null
}
