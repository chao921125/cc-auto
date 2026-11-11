package org.autojs.core.runtime.api

import android.graphics.Bitmap
import org.autojs.core.api.ApiProvider
import org.autojs.core.api.ScriptScope
import org.autojs.core.runtime.canvas.ScriptCanvas
import org.autojs.core.runtime.canvas.ScriptPaint
import org.autojs.core.runtime.canvas.ScriptPath
import org.mozilla.javascript.BaseFunction
import org.mozilla.javascript.Context
import org.mozilla.javascript.Scriptable
import org.mozilla.javascript.ScriptableObject

/**
 * canvas 命名空间：2D 画布（对齐 AutoJS Pro 一代 canvas 模块）。
 *
 * - canvas.create(w, h) / canvas.fromBitmap(bmp)：创建位图画布（CcAuto 扩展入口，
 *   Pro 中位图画布经 Java 交互 new Canvas(bitmap) 获得，本引擎沙箱不开放 Java 交互，故提供工厂函数）。
 * - ui/floaty 的 <canvas> 控件 draw 回调：视图画布（与 Pro 一致）。
 * - 全局 Paint / Path 构造器：对齐 Pro 脚本 `new Paint()` / `new Path()` 写法，
 *   并附带 Paint.Style / Paint.Align / Paint.Cap / Paint.Join 常量。
 */
class CanvasApiProvider : ApiProvider {
    override val name = "canvas"

    override fun install(scope: ScriptScope) {
        scope.defineObject("canvas") {
            function("create") { args ->
                val w = (args.getOrNull(0) as? Number)?.toInt() ?: return@function null
                val h = (args.getOrNull(1) as? Number)?.toInt() ?: return@function null
                if (w <= 0 || h <= 0) return@function null
                ScriptCanvas(Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888))
            }
            function("fromBitmap") { args ->
                val bmp = Context.jsToJava(args.getOrNull(0), Bitmap::class.java) as? Bitmap ?: return@function null
                val mutable = if (bmp.isMutable) bmp else bmp.copy(Bitmap.Config.ARGB_8888, true)
                ScriptCanvas(mutable)
            }
            function("paint") { _ -> ScriptPaint() }
            function("path") { _ -> ScriptPath() }
        }

        // 全局构造器：new Paint() / Paint() 均可
        val paintCtor = javaConstructor { ScriptPaint() }
        attachConstants(scope, paintCtor, "Style", "FILL", "STROKE", "FILL_AND_STROKE")
        attachConstants(scope, paintCtor, "Align", "LEFT", "CENTER", "RIGHT")
        attachConstants(scope, paintCtor, "Cap", "BUTT", "ROUND", "SQUARE")
        attachConstants(scope, paintCtor, "Join", "MITER", "ROUND", "BEVEL")
        scope.defineProperty("Paint", paintCtor)
        scope.defineProperty("Path", javaConstructor { ScriptPath() })
    }

    private fun javaConstructor(create: () -> Any): BaseFunction = object : BaseFunction() {
        override fun call(cx: Context, s: Scriptable, thisObj: Scriptable, args: Array<out Any?>): Any = create()
        override fun construct(cx: Context, s: Scriptable, args: Array<out Any?>): Scriptable =
            Context.javaToJS(create(), s) as Scriptable
    }

    /** 形如 Paint.Style.FILL 的常量对象；值为字符串，ScriptPaint 的 setter 同时接受字符串与序号。 */
    private fun attachConstants(scope: ScriptScope, ctor: BaseFunction, name: String, vararg values: String) {
        val members = HashMap<String, Any?>()
        values.forEach { v -> members[v] = v }
        val obj = scope.createDynamicObject(members) ?: return
        ScriptableObject.defineProperty(ctor, name, obj, ScriptableObject.READONLY)
    }
}