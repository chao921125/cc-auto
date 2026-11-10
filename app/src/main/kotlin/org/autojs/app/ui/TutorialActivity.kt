package org.autojs.app.ui

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import org.autojs.app.R
import org.autojs.app.engine.EngineManager
import org.autojs.app.engine.LogEntryBus

class TutorialActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tutorial)

        val topic = TutorialData.topics[intent.getIntExtra("topic_index", 0)]

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = topic.title
        toolbar.setTitleTextColor(0xFFFFFFFF.toInt())
        toolbar.setNavigationIcon(android.R.drawable.ic_menu_revert)
        toolbar.setNavigationOnClickListener { finish() }

        findViewById<TextView>(R.id.tutorial_body).text = topic.body

        val runBtn = findViewById<Button>(R.id.btn_run_sample)
        val sample = topic.sample
        if (sample == null) {
            runBtn.visibility = Button.GONE
        } else {
            runBtn.setOnClickListener { runSample(sample) }
        }
    }

    private fun runSample(assetName: String) {
        val code = try {
            assets.open("scripts/$assetName").bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            Toast.makeText(this, "读取示例失败: ${e.message}", Toast.LENGTH_SHORT).show()
            return
        }
        val manager = EngineManager.get(applicationContext)
        val id = manager.executeCode(code, assetName)
        manager.getRuntime(id)?.onLog = { level, msg -> LogEntryBus.emit(level, msg) }
        Toast.makeText(this, "已启动示例，可在「管理」查看日志", Toast.LENGTH_SHORT).show()
    }
}
