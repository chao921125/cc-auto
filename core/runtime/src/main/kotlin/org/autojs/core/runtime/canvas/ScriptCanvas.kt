package org.autojs.core.runtime.canvas

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.PorterDuff
import android.graphics.RectF
import org.mozilla.javascript.Context
import org.mozilla.javascript.Scriptable
import org.mozilla.javascript.ScriptableObject

/**
 * 脚本画布：包装 android.graphics.Canvas（对齐 AutoJS Pro 一代 canvas 模块）。
 *
 * 两种来源：
 * 1. 位图画布：canvas.create(w, h) / canvas.fromBitmap(bmp)，绘制结果可用 getBitmap() 取出；
 * 2. 视图画布：ui/floaty 的 <canvas> 控件 draw 回调参数（无位图，getBitmap() 返回 null）。
 *
 * 矩形参数支持两种形式：坐标 (left, top, right, bottom) 或对象 {left, top, right, bottom} / 数组 [l, t, r, b]。
 */
class ScriptCanvas(val raw: Canvas, private val backingBitmap: Bitmap?) {

    constructor(bitmap: Bitmap) : this(Canvas(bitmap), bitmap)

    fun getWidth(): Int = raw.width
    fun getHeight(): Int = raw.height

    // ---- 颜色填充 ----
    fun drawRGB(r: Int, g: Int, b: Int) { raw.drawRGB(r, g, b) }
    fun drawARGB(a: Int, r: Int, g: Int, b: Int) { raw.drawARGB(a, r, g, b) }
    fun drawColor(color: Int) { raw.drawColor(color) }
    fun drawColor(color: String) { raw.drawColor(android.graphics.Color.parseColor(color)) }
    fun drawColor(color: Int, mode: String) {
        raw.drawColor(color, porterDuffMode(mode))
    }
    fun drawPaint(paint: ScriptPaint) { raw.drawPaint(paint.raw) }

    // ---- 点与线 ----
    fun drawPoint(x: Double, y: Double, paint: ScriptPaint) { raw.drawPoint(x.toFloat(), y.toFloat(), paint.raw) }
    fun drawPoints(pts: FloatArray, paint: ScriptPaint) { raw.drawPoints(pts, paint.raw) }
    fun drawLine(startX: Double, startY: Double, stopX: Double, stopY: Double, paint: ScriptPaint) {
        raw.drawLine(startX.toFloat(), startY.toFloat(), stopX.toFloat(), stopY.toFloat(), paint.raw)
    }
    fun drawLines(pts: FloatArray, paint: ScriptPaint) { raw.drawLines(pts, paint.raw) }

    // ---- 矩形 / 圆角矩形 ----
    fun drawRect(left: Double, top: Double, right: Double, bottom: Double, paint: ScriptPaint) {
        raw.drawRect(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), paint.raw)
    }
    fun drawRect(r: Scriptable, paint: ScriptPaint) {
        rectOf(r)?.let { raw.drawRect(it, paint.raw) }
    }
    fun drawRoundRect(left: Double, top: Double, right: Double, bottom: Double, rx: Double, ry: Double, paint: ScriptPaint) {
        raw.drawRoundRect(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), rx.toFloat(), ry.toFloat(), paint.raw)
    }
    fun drawRoundRect(r: Scriptable, rx: Double, ry: Double, paint: ScriptPaint) {
        rectOf(r)?.let { raw.drawRoundRect(it, rx.toFloat(), ry.toFloat(), paint.raw) }
    }

    // ---- 圆 / 椭圆 / 弧 ----
    fun drawCircle(cx: Double, cy: Double, radius: Double, paint: ScriptPaint) {
        raw.drawCircle(cx.toFloat(), cy.toFloat(), radius.toFloat(), paint.raw)
    }
    fun drawOval(left: Double, top: Double, right: Double, bottom: Double, paint: ScriptPaint) {
        raw.drawOval(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), paint.raw)
    }
    fun drawOval(oval: Scriptable, paint: ScriptPaint) {
        rectOf(oval)?.let { raw.drawOval(it, paint.raw) }
    }
    fun drawArc(left: Double, top: Double, right: Double, bottom: Double, startAngle: Double, sweepAngle: Double, useCenter: Boolean, paint: ScriptPaint) {
        raw.drawArc(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), startAngle.toFloat(), sweepAngle.toFloat(), useCenter, paint.raw)
    }
    fun drawArc(oval: Scriptable, startAngle: Double, sweepAngle: Double, useCenter: Boolean, paint: ScriptPaint) {
        rectOf(oval)?.let { raw.drawArc(it, startAngle.toFloat(), sweepAngle.toFloat(), useCenter, paint.raw) }
    }

    // ---- 路径 / 位图 / 文字 ----
    fun drawPath(path: ScriptPath, paint: ScriptPaint) { raw.drawPath(path.raw, paint.raw) }
    fun drawBitmap(bitmap: Bitmap, left: Double, top: Double) {
        raw.drawBitmap(bitmap, left.toFloat(), top.toFloat(), null)
    }
    fun drawBitmap(bitmap: Bitmap, left: Double, top: Double, paint: ScriptPaint) {
        raw.drawBitmap(bitmap, left.toFloat(), top.toFloat(), paint.raw)
    }
    fun drawText(text: String, x: Double, y: Double, paint: ScriptPaint) {
        raw.drawText(text, x.toFloat(), y.toFloat(), paint.raw)
    }
    fun drawTextOnPath(text: String, path: ScriptPath, hOffset: Double, vOffset: Double, paint: ScriptPaint) {
        raw.drawTextOnPath(text, path.raw, hOffset.toFloat(), vOffset.toFloat(), paint.raw)
    }

    // ---- 矩阵变换 ----
    fun translate(dx: Double, dy: Double) { raw.translate(dx.toFloat(), dy.toFloat()) }
    fun scale(sx: Double, sy: Double) { raw.scale(sx.toFloat(), sy.toFloat()) }
    fun scale(sx: Double, sy: Double, px: Double, py: Double) { raw.scale(sx.toFloat(), sy.toFloat(), px.toFloat(), py.toFloat()) }
    fun rotate(degrees: Double) { raw.rotate(degrees.toFloat()) }
    fun rotate(degrees: Double, px: Double, py: Double) { raw.rotate(degrees.toFloat(), px.toFloat(), py.toFloat()) }
    fun skew(sx: Double, sy: Double) { raw.skew(sx.toFloat(), sy.toFloat()) }

    // ---- 状态与裁剪（Android Canvas 标准能力） ----
    fun save(): Int = raw.save()
    fun restore() { raw.restore() }
    fun clipRect(left: Double, top: Double, right: Double, bottom: Double): Boolean =
        raw.clipRect(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat())

    /** 取出画布位图（视图画布返回 null）。CcAuto 扩展，便于 images.save / 找色等后续处理。 */
    fun getBitmap(): Bitmap? = backingBitmap

    private fun porterDuffMode(mode: String): PorterDuff.Mode =
        runCatching { PorterDuff.Mode.valueOf(mode.uppercase()) }.getOrDefault(PorterDuff.Mode.SRC_OVER)

    /** 矩形对象 {left,top,right,bottom} 或数组 [l,t,r,b] → RectF。 */
    private fun rectOf(r: Scriptable): RectF? {
        fun num(key: Any): Float = when (val v = if (key is Int) r.get(key, r) else ScriptableObject.getProperty(r, key.toString())) {
            is Number -> v.toFloat()
            else -> Context.toNumber(v).toFloat()
        }
        return runCatching {
            if (r is org.mozilla.javascript.NativeArray || ScriptableObject.getProperty(r, "length") is Number) {
                RectF(num(0), num(1), num(2), num(3))
            } else {
                RectF(num("left"), num("top"), num("right"), num("bottom"))
            }
        }.getOrNull()
    }
}