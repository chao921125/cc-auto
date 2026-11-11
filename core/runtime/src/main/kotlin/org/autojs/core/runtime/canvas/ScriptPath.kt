package org.autojs.core.runtime.canvas

import android.graphics.Path

/**
 * 脚本路径：包装 android.graphics.Path，供 canvas.drawPath / drawTextOnPath 使用。
 * 对齐 AutoJS Pro 的 Path 用法（new Path() / moveTo / lineTo / quadTo / cubicTo / arcTo / close）。
 */
class ScriptPath {
    val raw: Path = Path()

    fun moveTo(x: Double, y: Double) { raw.moveTo(x.toFloat(), y.toFloat()) }
    fun lineTo(x: Double, y: Double) { raw.lineTo(x.toFloat(), y.toFloat()) }
    fun quadTo(x1: Double, y1: Double, x2: Double, y2: Double) {
        raw.quadTo(x1.toFloat(), y1.toFloat(), x2.toFloat(), y2.toFloat())
    }
    fun cubicTo(x1: Double, y1: Double, x2: Double, y2: Double, x3: Double, y3: Double) {
        raw.cubicTo(x1.toFloat(), y1.toFloat(), x2.toFloat(), y2.toFloat(), x3.toFloat(), y3.toFloat())
    }
    fun arcTo(left: Double, top: Double, right: Double, bottom: Double, startAngle: Double, sweepAngle: Double, forceMoveTo: Boolean) {
        raw.arcTo(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), startAngle.toFloat(), sweepAngle.toFloat(), forceMoveTo)
    }
    fun addRect(left: Double, top: Double, right: Double, bottom: Double) {
        raw.addRect(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), Path.Direction.CW)
    }
    fun addRoundRect(left: Double, top: Double, right: Double, bottom: Double, rx: Double, ry: Double) {
        raw.addRoundRect(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), rx.toFloat(), ry.toFloat(), Path.Direction.CW)
    }
    fun addOval(left: Double, top: Double, right: Double, bottom: Double) {
        raw.addOval(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), Path.Direction.CW)
    }
    fun addCircle(cx: Double, cy: Double, radius: Double) {
        raw.addCircle(cx.toFloat(), cy.toFloat(), radius.toFloat(), Path.Direction.CW)
    }
    fun close() { raw.close() }
    fun reset() { raw.reset() }
    fun offset(dx: Double, dy: Double) { raw.offset(dx.toFloat(), dy.toFloat()) }
}