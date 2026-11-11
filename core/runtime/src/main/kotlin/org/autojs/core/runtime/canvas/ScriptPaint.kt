package org.autojs.core.runtime.canvas

import android.graphics.Paint

/**
 * 脚本画笔：包装 android.graphics.Paint。
 * 对齐 AutoJS Pro 的 Paint 用法（new Paint() / paint.setColor / setStyle 等）。
 * 枚举参数同时接受字符串（"FILL"）与序号（0/1/2），兼容 Paint.Style.FILL 常量对象。
 */
class ScriptPaint {
    val raw: Paint = Paint()

    fun setColor(color: Int) { raw.color = color }
    fun setColor(color: String) { raw.color = android.graphics.Color.parseColor(color) }
    fun setARGB(a: Int, r: Int, g: Int, b: Int) { raw.setARGB(a, r, g, b) }
    fun setAlpha(alpha: Int) { raw.alpha = alpha }
    fun getColor(): Int = raw.color
    fun getAlpha(): Int = raw.alpha

    fun setStyle(style: Any?) {
        raw.style = when (style) {
            is Number -> Paint.Style.entries.getOrElse(style.toInt()) { Paint.Style.FILL }
            else -> runCatching { Paint.Style.valueOf(style.toString().uppercase()) }.getOrDefault(Paint.Style.FILL)
        }
    }
    fun getStyle(): String = raw.style.name

    fun setStrokeWidth(width: Double) { raw.strokeWidth = width.toFloat() }
    fun getStrokeWidth(): Double = raw.strokeWidth.toDouble()

    fun setStrokeCap(cap: Any?) {
        raw.strokeCap = when (cap) {
            is Number -> Paint.Cap.entries.getOrElse(cap.toInt()) { Paint.Cap.BUTT }
            else -> runCatching { Paint.Cap.valueOf(cap.toString().uppercase()) }.getOrDefault(Paint.Cap.BUTT)
        }
    }

    fun setStrokeJoin(join: Any?) {
        raw.strokeJoin = when (join) {
            is Number -> Paint.Join.entries.getOrElse(join.toInt()) { Paint.Join.MITER }
            else -> runCatching { Paint.Join.valueOf(join.toString().uppercase()) }.getOrDefault(Paint.Join.MITER)
        }
    }

    fun setAntiAlias(aa: Boolean) { raw.isAntiAlias = aa }
    fun isAntiAlias(): Boolean = raw.isAntiAlias
    fun setDither(dither: Boolean) { raw.isDither = dither }

    fun setTextSize(size: Double) { raw.textSize = size.toFloat() }
    fun getTextSize(): Double = raw.textSize.toDouble()

    fun setTextAlign(align: Any?) {
        raw.textAlign = when (align) {
            is Number -> Paint.Align.entries.getOrElse(align.toInt()) { Paint.Align.LEFT }
            else -> runCatching { Paint.Align.valueOf(align.toString().uppercase()) }.getOrDefault(Paint.Align.LEFT)
        }
    }

    fun setTextScaleX(scaleX: Double) { raw.textScaleX = scaleX.toFloat() }
    fun setTextSkewX(skewX: Double) { raw.textSkewX = skewX.toFloat() }
    fun setFakeBoldText(bold: Boolean) { raw.isFakeBoldText = bold }
    fun setUnderlineText(underline: Boolean) { raw.isUnderlineText = underline }
    fun setStrikeThruText(strike: Boolean) { raw.isStrikeThruText = strike }
    fun setLetterSpacing(spacing: Double) { raw.letterSpacing = spacing.toFloat() }

    fun setShadowLayer(radius: Double, dx: Double, dy: Double, color: Int) {
        raw.setShadowLayer(radius.toFloat(), dx.toFloat(), dy.toFloat(), color)
    }
    fun clearShadowLayer() { raw.clearShadowLayer() }

    /** 文字宽度测量（排版常用，Pro 经 Java 交互同名方法实现）。 */
    fun measureText(text: String): Double = raw.measureText(text).toDouble()
}