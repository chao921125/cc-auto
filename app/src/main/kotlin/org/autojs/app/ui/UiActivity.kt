package org.autojs.app.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import org.autojs.core.runtime.UiBridge
import org.autojs.core.runtime.UiHost
import org.autojs.toolkit.floaty.XmlViewRenderer
import java.util.HashMap
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * 脚本 UI 宿主 Activity：渲染 ui.layout 提交的 XML 并回传 id 绑定表。
 */
class UiActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        instance = this
        val xml = pendingXml
        var map: Map<String, View> = emptyMap()
        if (xml != null) {
            val collected = HashMap<String, View>()
            try {
                val root = XmlViewRenderer(this).render(xml, null, collected)
                root.layoutParams = android.view.ViewGroup.LayoutParams(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT
                )
                root.fitsSystemWindows = true
                setContentView(root)
                map = collected
            } catch (e: Exception) {
                finish()
            }
        }
        val cb = rendered
        rendered = null
        cb?.invoke(map)
    }

    override fun onDestroy() {
        if (instance === this) instance = null
        super.onDestroy()
    }

    companion object {
        @Volatile var instance: UiActivity? = null
        @Volatile var pendingXml: String? = null
        @Volatile var rendered: ((Map<String, View>) -> Unit)? = null
    }
}

/** UiBridge 宿主实现：启动 UiActivity 并等待渲染完成。 */
class UiHostImpl(private val context: Context) : UiHost {

    override fun show(xml: String, timeoutMs: Long): Map<String, View>? {
        val latch = CountDownLatch(1)
        var result: Map<String, View>? = null
        UiBridge.mainHandler.post {
            UiActivity.pendingXml = xml
            UiActivity.rendered = { map ->
                result = map
                latch.countDown()
            }
            context.startActivity(
                Intent(context, UiActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
        latch.await(timeoutMs, TimeUnit.MILLISECONDS)
        return result
    }

    override fun finish() {
        UiBridge.mainHandler.post { UiActivity.instance?.finish() }
    }
}
