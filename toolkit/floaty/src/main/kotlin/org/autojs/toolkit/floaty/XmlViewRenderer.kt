package org.autojs.toolkit.floaty

import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import android.util.Xml
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.FrameLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.ProgressBar
import android.widget.CompoundButton
import org.xmlpull.v1.XmlPullParser

/**
 * XML → View 渲染器：将脚本内 XML 字符串解析为原生 View。
 * 用于 floaty.window(xml) 和 ui.layout(xml)。
 *
 * 支持标签：frame/linear/vertical/horizontal/relative/text/button/input/edittext/img/
 *          checkbox/switch/scroll/list/progressbar/seekbar/spinner/radiogroup
 * 支持属性：id, text, hint, w/h(width/height), padding, margin*, layout_weight, bg,
 *          gravity, textColor, textSize, max, progress, checked, onClick
 */
class XmlViewRenderer(private val context: Context) {

    /** 渲染并可选收集 id → View 绑定表。 */
    fun render(xml: String, parent: ViewGroup? = null, idMap: MutableMap<String, View>? = null): View {
        val parser = Xml.newPullParser()
        parser.setInput(xml.reader())
        return parseView(parser, parent, idMap)
    }

    private fun parseView(parser: XmlPullParser, parent: ViewGroup?, idMap: MutableMap<String, View>?): View {
        var event = parser.eventType
        while (event != XmlPullParser.END_DOCUMENT) {
            if (event == XmlPullParser.START_TAG) {
                val tagName = parser.name
                val view = createView(tagName)
                // 预置与父容器匹配的 LayoutParams，保证 weight/margin 生效
                view.layoutParams = when (parent) {
                    is LinearLayout -> LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                    is FrameLayout -> FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                    is RelativeLayout -> RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                    else -> ViewGroup.MarginLayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                }
                var idName: String? = null
                for (i in 0 until parser.attributeCount) {
                    val attrName = parser.getAttributeName(i)
                    val attrValue = parser.getAttributeValue(i)
                    if (attrName == "id" || attrName == "android:id") {
                        idName = attrValue.removePrefix("@+id/").removePrefix("@id/")
                    } else {
                        applyAttribute(view, attrName, attrValue)
                    }
                }
                idName?.let { idMap?.put(it, view) }
                if (view is ViewGroup) {
                    parseChildren(parser, view, idMap)
                } else {
                    skipChildren(parser)
                }
                return view
            }
            event = parser.next()
        }
        throw IllegalArgumentException("No root view found in XML")
    }

    private fun parseChildren(parser: XmlPullParser, parent: ViewGroup, idMap: MutableMap<String, View>?) {
        var event = parser.next()
        while (event != XmlPullParser.END_DOCUMENT) {
            when (event) {
                XmlPullParser.START_TAG -> parent.addView(parseView(parser, parent, idMap))
                XmlPullParser.END_TAG -> return
            }
            event = parser.next()
        }
    }

    private fun skipChildren(parser: XmlPullParser) {
        var depth = 1
        while (depth > 0) {
            when (val event = parser.next()) {
                XmlPullParser.START_TAG -> depth++
                XmlPullParser.END_TAG -> depth--
                XmlPullParser.END_DOCUMENT -> return
            }
        }
    }

    private fun createView(tagName: String): View = when (tagName) {
        "frame" -> FrameLayout(context)
        "linear", "vertical", "horizontal" -> LinearLayout(context).apply {
            orientation = if (tagName == "horizontal") LinearLayout.HORIZONTAL else LinearLayout.VERTICAL
        }
        "relative" -> RelativeLayout(context)
        "text" -> TextView(context)
        "button" -> android.widget.Button(context)
        "input", "edittext" -> android.widget.EditText(context)
        "img" -> android.widget.ImageView(context)
        "checkbox" -> android.widget.CheckBox(context)
        "switch" -> android.widget.Switch(context)
        "scroll" -> android.widget.ScrollView(context)
        "list" -> android.widget.ListView(context)
        "progressbar" -> ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal)
        "seekbar" -> android.widget.SeekBar(context)
        "spinner" -> android.widget.Spinner(context)
        "radiogroup" -> android.widget.RadioGroup(context)
        else -> TextView(context)
    }

    private fun applyAttribute(view: View, name: String, value: String) {
        when (name) {
            "text" -> (view as? TextView)?.text = value
            "hint" -> (view as? android.widget.EditText)?.hint = value
            "padding" -> {
                val p = dp(value)
                view.setPadding(p, p, p, p)
            }
            "w", "width" -> updateSize(view, dp(value), null)
            "h", "height" -> updateSize(view, null, dp(value))
            "marginTop" -> updateMargin(view, top = dp(value))
            "marginBottom" -> updateMargin(view, bottom = dp(value))
            "marginLeft" -> updateMargin(view, left = dp(value))
            "marginRight" -> updateMargin(view, right = dp(value))
            "layout_weight" -> (view.layoutParams as? LinearLayout.LayoutParams)?.weight = value.toFloatOrNull() ?: 0f
            "bg", "background" -> if (value.startsWith("#")) view.setBackgroundColor(Color.parseColor(value))
            "textColor" -> (view as? TextView)?.setTextColor(Color.parseColor(value))
            "textSize" -> (view as? TextView)?.apply {
                val sp = value.removeSuffix("sp").toFloatOrNull()
                if (sp != null) setTextSize(TypedValue.COMPLEX_UNIT_SP, sp)
            }
            "gravity" -> (view as? TextView)?.gravity = parseGravity(value)
            "max" -> (view as? ProgressBar)?.max = value.toIntOrNull() ?: 100
            "progress" -> (view as? ProgressBar)?.progress = value.toIntOrNull() ?: 0
            "checked" -> (view as? CompoundButton)?.isChecked = value == "true" || value == "1"
            "id", "android:id" -> {} // 已在调用处处理
        }
    }

    private fun dp(value: String): Int {
        if (value == "wrap_content" || value == "auto") return ViewGroup.LayoutParams.WRAP_CONTENT
        if (value == "match_parent" || value == "*") return ViewGroup.LayoutParams.MATCH_PARENT
        val num = value.removeSuffix("dp").toFloatOrNull() ?: return ViewGroup.LayoutParams.WRAP_CONTENT
        return (num * context.resources.displayMetrics.density).toInt()
    }

    private fun updateSize(view: View, w: Int?, h: Int?) {
        val lp = view.layoutParams ?: ViewGroup.MarginLayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        lp as ViewGroup.MarginLayoutParams
        if (w != null) lp.width = w
        if (h != null) lp.height = h
        view.layoutParams = lp
    }

    private fun updateMargin(
        view: View, left: Int = Int.MIN_VALUE, top: Int = Int.MIN_VALUE,
        right: Int = Int.MIN_VALUE, bottom: Int = Int.MIN_VALUE
    ) {
        val lp = view.layoutParams as? ViewGroup.MarginLayoutParams ?: return
        if (left != Int.MIN_VALUE) lp.leftMargin = left
        if (top != Int.MIN_VALUE) lp.topMargin = top
        if (right != Int.MIN_VALUE) lp.rightMargin = right
        if (bottom != Int.MIN_VALUE) lp.bottomMargin = bottom
        view.layoutParams = lp
    }

    private fun parseGravity(value: String): Int {
        var g = 0
        if (value.contains("center")) g = g or Gravity.CENTER
        if (value.contains("left")) g = g or Gravity.LEFT
        if (value.contains("right")) g = g or Gravity.RIGHT
        if (value.contains("top")) g = g or Gravity.TOP
        if (value.contains("bottom")) g = g or Gravity.BOTTOM
        return g
    }
}
