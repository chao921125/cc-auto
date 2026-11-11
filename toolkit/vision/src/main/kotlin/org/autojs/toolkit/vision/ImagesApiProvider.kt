package org.autojs.toolkit.vision

import android.graphics.Bitmap
import org.autojs.core.api.ApiProvider
import org.autojs.core.api.ScriptScope
import org.mozilla.javascript.Context

/**
 * images 命名空间 API：截图、找图、找色、图片操作。
 *
 * 注意：JS 侧传入的 Bitmap/对象/数组是 Rhino 包装类型（NativeJavaObject/NativeObject/NativeArray），
 * 必须经 Context.jsToJava 解包，不能直接 as? 强转。
 */
class ImagesApiProvider : ApiProvider {
    override val name = "images"

    private val captureManager = ScreenCaptureManager()
    private val matcher = ImageMatcher()
    private val colorFinder = ColorFinder()

    override fun install(scope: ScriptScope) {
        scope.defineObject("images") {
            function("requestScreenCapture") { _ -> captureManager.isReady() }
            function("captureScreen") { _ -> captureManager.capture() }
            function("findImage") { args ->
                val src = bitmapOf(args.getOrNull(0)) ?: return@function null
                val tpl = bitmapOf(args.getOrNull(1)) ?: return@function null
                val opts = mapOf(args.getOrNull(2))
                val region = intListOf(opts["region"])
                val threshold = (opts["threshold"] as? Number)?.toDouble() ?: 0.8
                matcher.findImage(src, tpl, region, threshold)
            }
            function("findColor") { args ->
                val bmp = bitmapOf(args.getOrNull(0)) ?: return@function null
                val color = (args.getOrNull(1) as? Number)?.toInt() ?: return@function null
                val opts = mapOf(args.getOrNull(2))
                val region = intListOf(opts["region"])
                val threshold = (opts["threshold"] as? Number)?.toInt() ?: 4
                colorFinder.findColor(bmp, color, region, threshold)
            }
            function("findMultiColors") { args ->
                val bmp = bitmapOf(args.getOrNull(0)) ?: return@function null
                val firstColor = (args.getOrNull(1) as? Number)?.toInt() ?: return@function null
                val offsets = (Context.jsToJava(args.getOrNull(2), List::class.java) as? List<*>)
                    ?.mapNotNull { intListOf(it) } ?: emptyList()
                colorFinder.findMultiColors(bmp, firstColor, offsets)
            }
            function("detectsColor") { args ->
                val bmp = bitmapOf(args.getOrNull(0)) ?: return@function false
                val x = (args.getOrNull(1) as? Number)?.toInt() ?: 0
                val y = (args.getOrNull(2) as? Number)?.toInt() ?: 0
                val color = (args.getOrNull(3) as? Number)?.toInt() ?: 0
                colorFinder.detectsColor(bmp, x, y, color)
            }
            function("crop") { args ->
                val bmp = bitmapOf(args.getOrNull(0)) ?: return@function null
                val region = intListOf(args.getOrNull(1)) ?: return@function null
                ImageMatcher.crop(bmp, region)
            }
            function("resize") { args ->
                val bmp = bitmapOf(args.getOrNull(0)) ?: return@function null
                val w = (args.getOrNull(1) as? Number)?.toInt() ?: 0
                val h = (args.getOrNull(2) as? Number)?.toInt() ?: 0
                ImageMatcher.resize(bmp, w, h)
            }
            function("pixel") { args ->
                val bmp = bitmapOf(args.getOrNull(0)) ?: return@function 0
                val x = (args.getOrNull(1) as? Number)?.toInt() ?: 0
                val y = (args.getOrNull(2) as? Number)?.toInt() ?: 0
                ImageMatcher.pixel(bmp, x, y)
            }
            function("read") { args ->
                val path = args.getOrNull(0)?.toString() ?: return@function null
                try { android.graphics.BitmapFactory.decodeFile(path) } catch (e: Exception) { null }
            }
            function("save") { args ->
                val bmp = bitmapOf(args.getOrNull(0)) ?: return@function false
                val path = args.getOrNull(1)?.toString() ?: return@function false
                try {
                    java.io.FileOutputStream(path).use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
                    true
                } catch (e: Exception) { false }
            }
            property("captureManager", captureManager)
        }
    }

    private fun bitmapOf(v: Any?): Bitmap? = Context.jsToJava(v, Bitmap::class.java) as? Bitmap

    @Suppress("UNCHECKED_CAST")
    private fun mapOf(v: Any?): Map<String, Any?> =
        (Context.jsToJava(v, Map::class.java) as? Map<String, Any?>) ?: emptyMap()

    private fun intListOf(v: Any?): IntArray? =
        (Context.jsToJava(v, List::class.java) as? List<*>)?.mapNotNull { (it as? Number)?.toInt() }?.toIntArray()
}

/**
 * ocr 命名空间 API：文字识别。
 */
class OcrApiProvider : ApiProvider {
    override val name = "ocr"
    private val engine = OcrEngine()

    override fun install(scope: ScriptScope) {
        scope.defineObject("ocr") {
            function("recognize") { args ->
                val bmp = Context.jsToJava(args.getOrNull(0), Bitmap::class.java) as? Bitmap
                    ?: return@function emptyList<Any>()
                kotlinx.coroutines.runBlocking { engine.recognize(bmp) }
            }
            function("close") { _ -> engine.close() }
        }
    }
}