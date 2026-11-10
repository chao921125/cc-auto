package org.autojs.core.runtime

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.KeyEvent
import org.autojs.core.api.KeyEventBus
import org.mozilla.javascript.BaseFunction
import org.mozilla.javascript.Context as JsContext
import org.mozilla.javascript.Scriptable

/** shell 执行结果，JS 侧以 r.code / r.result / r.error 访问。 */
class ShellOut(val code: Int, val result: String, val error: String)

/** 内置 shell 执行器（与 toolkit 版本行为一致，供全局 shell() 使用）。 */
object BuiltinShell {
    @Volatile
    private var rootCached: Boolean? = null

    fun exec(cmd: String, root: Boolean): ShellOut {
        return try {
            val process = if (root) {
                Runtime.getRuntime().exec(arrayOf("su"))
            } else {
                Runtime.getRuntime().exec(arrayOf("sh", "-c", cmd))
            }
            if (root) {
                process.outputStream.write("$cmd\nexit\n".toByteArray())
                process.outputStream.flush()
            }
            val out = process.inputStream.bufferedReader().readText()
            val err = process.errorStream.bufferedReader().readText()
            val code = process.waitFor()
            ShellOut(code, out.trim(), err.trim())
        } catch (e: Exception) {
            ShellOut(-1, "", e.message ?: "exec failed")
        }
    }

    fun isRootAvailable(): Boolean {
        rootCached?.let { return it }
        val ok = try {
            Runtime.getRuntime().exec(arrayOf("su", "-c", "id")).waitFor() == 0
        } catch (e: Exception) { false }
        rootCached = ok
        return ok
    }
}

/**
 * Root 按键读取器：`su -c getevent -l` 文本流解析 EV_KEY，分发到 KeyEventBus。
 * 作为无障碍按键过滤的兜底通道（root 设备上可捕获更多按键来源）。
 */
object RootKeyEventReader {
    @Volatile
    private var process: Process? = null

    fun isRunning(): Boolean = process != null

    fun start(): Boolean {
        if (process != null) return true
        return try {
            val p = Runtime.getRuntime().exec(arrayOf("su", "-c", "getevent -l"))
            process = p
            Thread {
                p.inputStream.bufferedReader().forEachLine { line -> parseAndDispatch(line) }
            }.apply { isDaemon = true; name = "root-getevent"; start() }
            true
        } catch (e: Exception) {
            process = null
            false
        }
    }

    fun stop() {
        process?.destroy()
        process = null
    }

    /** 行格式示例："/dev/input/event1: EV_KEY KEY_VOLUMEUP DOWN" */
    private fun parseAndDispatch(line: String) {
        val parts = line.split(" ").filter { it.isNotBlank() }
        if (parts.size < 4) return
        if (parts[1] != "EV_KEY") return
        val action = when (parts[3]) {
            "DOWN" -> KeyEvent.ACTION_DOWN
            "UP" -> KeyEvent.ACTION_UP
            "REPEAT" -> KeyEvent.ACTION_DOWN
            else -> return
        }
        val code = KeyEvent.keyCodeFromString(parts[2])
        if (code < 0) return
        KeyEventBus.dispatch(KeyEvent(action, code))
    }
}

/** 全局 shell：既可调用 shell(cmd[,root])，也带 .exec/.isRootAvailable 成员。 */
class ShellCallable : BaseFunction() {
    override fun call(cx: JsContext, scope: Scriptable, thisObj: Scriptable, args: Array<out Any?>): Any? {
        val cmd = args.getOrNull(0)?.toString() ?: ""
        val root = args.getOrNull(1) == true
        return BuiltinShell.exec(cmd, root)
    }

    override fun get(name: String, start: Scriptable): Any? = when (name) {
        "exec" -> object : BaseFunction() {
            override fun call(cx: JsContext, s: Scriptable, t: Scriptable, args: Array<out Any?>): Any? =
                BuiltinShell.exec(args.getOrNull(0)?.toString() ?: "", args.getOrNull(1) == true)
        }
        "isRootAvailable" -> object : BaseFunction() {
            override fun call(cx: JsContext, s: Scriptable, t: Scriptable, args: Array<out Any?>): Any? =
                BuiltinShell.isRootAvailable()
        }
        else -> super.get(name, start)
    }
}

/** 剪贴板读写（脚本前台时可读；部分系统限制后台读取）。 */
fun setClipBoard(text: String) {
    val ctx = AppRuntime.context ?: return
    val cm = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return
    cm.setPrimaryClip(ClipData.newPlainText("CcAuto", text))
}

fun getClipBoard(): String {
    val ctx = AppRuntime.context ?: return ""
    val cm = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return ""
    return cm.primaryClip?.getItemAt(0)?.coerceToText(ctx)?.toString() ?: ""
}
