package org.autojs.core.runtime

/** 键名 → Android keyCode（events 与 keys 模块共用）。 */
object KeyCodes {
    fun of(v: Any?): Int? = when (v) {
        is Number -> v.toInt()
        is String -> when (v.lowercase()) {
            "back" -> android.view.KeyEvent.KEYCODE_BACK
            "home" -> android.view.KeyEvent.KEYCODE_HOME
            "menu" -> android.view.KeyEvent.KEYCODE_MENU
            "power" -> android.view.KeyEvent.KEYCODE_POWER
            "volume_up", "volup", "volumeup" -> android.view.KeyEvent.KEYCODE_VOLUME_UP
            "volume_down", "voldown", "volumedown" -> android.view.KeyEvent.KEYCODE_VOLUME_DOWN
            "volume_mute" -> android.view.KeyEvent.KEYCODE_VOLUME_MUTE
            "enter" -> android.view.KeyEvent.KEYCODE_ENTER
            "del", "delete" -> android.view.KeyEvent.KEYCODE_DEL
            "space" -> android.view.KeyEvent.KEYCODE_SPACE
            "tab" -> android.view.KeyEvent.KEYCODE_TAB
            "camera" -> android.view.KeyEvent.KEYCODE_CAMERA
            "app_switch" -> android.view.KeyEvent.KEYCODE_APP_SWITCH
            "dpad_up" -> android.view.KeyEvent.KEYCODE_DPAD_UP
            "dpad_down" -> android.view.KeyEvent.KEYCODE_DPAD_DOWN
            "dpad_left" -> android.view.KeyEvent.KEYCODE_DPAD_LEFT
            "dpad_right" -> android.view.KeyEvent.KEYCODE_DPAD_RIGHT
            "dpad_center" -> android.view.KeyEvent.KEYCODE_DPAD_CENTER
            else -> v.toIntOrNull()
        }
        else -> null
    }
}
