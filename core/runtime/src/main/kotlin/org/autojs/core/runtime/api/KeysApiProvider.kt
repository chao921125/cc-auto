package org.autojs.core.runtime.api

import android.content.Context
import android.media.AudioManager
import android.view.KeyEvent
import org.autojs.core.api.ApiProvider
import org.autojs.core.api.ScriptScope
import org.autojs.core.runtime.AppRuntime
import org.autojs.core.runtime.BuiltinShell
import org.autojs.core.runtime.KeyCodes

/**
 * keys 命名空间：按键模拟。
 * - 音量键：AudioManager（免 root 可靠通道）
 * - 其他按键：su -c input keyevent（root）→ input keyevent（部分环境）→ 失败返回 false
 */
class KeysApiProvider : ApiProvider {
    override val name = "keys"

    override fun install(scope: ScriptScope) {
        scope.defineObject("keys") {
            function("press") { args -> press(args.getOrNull(0), long = false) }
            function("longPress") { args -> press(args.getOrNull(0), long = true) }
            function("down") { args -> raw(KeyCodes.of(args.getOrNull(0)), "--down") }
            function("up") { args -> raw(KeyCodes.of(args.getOrNull(0)), "--up") }
        }
    }

    private fun press(v: Any?, long: Boolean): Boolean {
        val code = KeyCodes.of(v) ?: return false
        val am = AppRuntime.context?.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        when (code) {
            KeyEvent.KEYCODE_VOLUME_UP -> {
                am?.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI)
                return true
            }
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                am?.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI)
                return true
            }
            KeyEvent.KEYCODE_VOLUME_MUTE -> {
                am?.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_TOGGLE_MUTE, AudioManager.FLAG_SHOW_UI)
                return true
            }
        }
        return raw(code, if (long) "--longpress" else null)
    }

    private fun raw(code: Int?, extra: String?): Boolean {
        code ?: return false
        val flag = if (extra != null) "$extra " else ""
        val cmd = "input keyevent ${flag}$code"
        if (BuiltinShell.exec(cmd, root = true).code == 0) return true
        return BuiltinShell.exec(cmd, root = false).code == 0
    }
}
