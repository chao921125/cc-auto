package org.autojs.core.runtime.canvas

import android.content.Context
import android.graphics.Canvas
import android.util.Log
import android.view.View

/**
 * <canvas> 控件：ui.layout / floaty.window XML 中的画布视图。
 * draw 回调在每次重绘时触发，参数为 ScriptCanvas（视图画布）。
 * 使用软件层以保证 drawTextOnPath 等全部绘制操作可用（硬件加速画布不支持部分操作）。
 */
class ScriptCanvasView(context: Context) : View(context) {

    var drawCallback: ((ScriptCanvas) -> Unit)? = null

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val cb = drawCallback ?: return
        try {
            cb(ScriptCanvas(canvas, null))
        } catch (e: Exception) {
            Log.w("CcAuto", "canvas draw 回调异常: ${e.message}")
        }
    }
}