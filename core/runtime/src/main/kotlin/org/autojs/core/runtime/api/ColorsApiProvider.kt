package org.autojs.core.runtime.api

import org.autojs.core.api.ApiProvider
import org.autojs.core.api.ScriptScope

/**
 * colors 命名空间：颜色解析与运算（对齐 AutoJS Pro colors 模块）。
 * 颜色值以 0xAARRGGBB 整型表示。
 */
class ColorsApiProvider : ApiProvider {
    override val name = "colors"

    override fun install(scope: ScriptScope) {
        scope.defineObject("colors") {
            function("parse") { args ->
                android.graphics.Color.parseColor(args.getOrNull(0)?.toString() ?: "#000000")
            }
            function("rgb") { args ->
                val r = (args.getOrNull(0) as? Number)?.toInt() ?: 0
                val g = (args.getOrNull(1) as? Number)?.toInt() ?: 0
                val b = (args.getOrNull(2) as? Number)?.toInt() ?: 0
                android.graphics.Color.rgb(r, g, b)
            }
            function("rgba") { args ->
                val r = (args.getOrNull(0) as? Number)?.toInt() ?: 0
                val g = (args.getOrNull(1) as? Number)?.toInt() ?: 0
                val b = (args.getOrNull(2) as? Number)?.toInt() ?: 0
                val a = (args.getOrNull(3) as? Number)?.toInt() ?: 255
                android.graphics.Color.argb(a, r, g, b)
            }
            function("red") { args -> android.graphics.Color.red(int(args)) }
            function("green") { args -> android.graphics.Color.green(int(args)) }
            function("blue") { args -> android.graphics.Color.blue(int(args)) }
            function("alpha") { args -> android.graphics.Color.alpha(int(args)) }
            function("hex") { args ->
                "#%06X".format(int(args) and 0xFFFFFF)
            }
            function("mix") { args ->
                val c1 = int(args, 0)
                val c2 = int(args, 1)
                val ratio = (args.getOrNull(2) as? Number)?.toDouble() ?: 0.5
                fun blend(ch1: Int, ch2: Int) = (ch1 + (ch2 - ch1) * ratio).toInt().coerceIn(0, 255)
                android.graphics.Color.argb(
                    blend(android.graphics.Color.alpha(c1), android.graphics.Color.alpha(c2)),
                    blend(android.graphics.Color.red(c1), android.graphics.Color.red(c2)),
                    blend(android.graphics.Color.green(c1), android.graphics.Color.green(c2)),
                    blend(android.graphics.Color.blue(c1), android.graphics.Color.blue(c2))
                )
            }
            function("darker") { args -> adjust(int(args), 0.8) }
            function("lighter") { args -> adjust(int(args), 1.25) }
            function("isSimilar") { args ->
                val c1 = int(args, 0); val c2 = int(args, 1)
                val threshold = (args.getOrNull(2) as? Number)?.toInt() ?: 16
                fun diff(f: (Int) -> Int) = kotlin.math.abs(f(c1) - f(c2))
                diff(android.graphics.Color::red) <= threshold &&
                    diff(android.graphics.Color::green) <= threshold &&
                    diff(android.graphics.Color::blue) <= threshold
            }
        }
    }

    private fun int(args: Array<out Any?>, index: Int = 0): Int =
        (args.getOrNull(index) as? Number)?.toInt() ?: 0

    private fun adjust(color: Int, factor: Double): Int {
        fun scale(ch: Int) = (ch * factor).toInt().coerceIn(0, 255)
        return android.graphics.Color.argb(
            android.graphics.Color.alpha(color),
            scale(android.graphics.Color.red(color)),
            scale(android.graphics.Color.green(color)),
            scale(android.graphics.Color.blue(color))
        )
    }
}
