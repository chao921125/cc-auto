package org.autojs.core.runtime.api

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.PowerManager
import org.autojs.core.api.ApiProvider
import org.autojs.core.api.ScriptScope
import org.autojs.core.runtime.AppRuntime
import org.autojs.core.runtime.BuiltinShell

/**
 * power_manager 命名空间：电源 / 电量 / 屏幕唤醒控制（对齐 AutoJS Pro）。
 *
 * - 电量信息经 ACTION_BATTERY_CHANGED 粘性广播读取，免权限、可实测
 * - wakeUp/sleep 经 `input keyevent`（sleep 通常需 root，失败返回 false）
 * - keepAwake 使用 PARTIAL_WAKE_LOCK（不锁屏，仅保持 CPU）
 */
class PowerManagerApiProvider : ApiProvider {
    override val name = "power_manager"

    private val pm: PowerManager?
        get() = AppRuntime.context?.getSystemService(Context.POWER_SERVICE) as? PowerManager

    private var wakelock: PowerManager.WakeLock? = null

    override fun install(scope: ScriptScope) {
        scope.defineObject("power_manager") {
            function("isScreenOn") { pm?.isInteractive ?: false }
            function("isInteractive") { pm?.isInteractive ?: false }

            function("wakeUp") {
                BuiltinShell.exec("input keyevent KEYCODE_WAKEUP", root = false).code == 0 ||
                    BuiltinShell.exec("input keyevent 224", root = true).code == 0
            }
            function("sleep") {
                BuiltinShell.exec("input keyevent KEYCODE_SLEEP", root = true).code == 0 ||
                    BuiltinShell.exec("input keyevent 223", root = false).code == 0
            }

            function("batteryLevel") { batteryInt(BatteryManager.EXTRA_LEVEL, -1) }
            function("batteryScale") { batteryInt(BatteryManager.EXTRA_SCALE, 100) }
            function("batteryStatus") { batteryInt(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_UNKNOWN) }
            function("health") { batteryInt(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_UNKNOWN) }
            function("plugType") { batteryInt(BatteryManager.EXTRA_PLUGGED, 0) }
            function("temperature") { batteryInt(BatteryManager.EXTRA_TEMPERATURE, 0) } // 0.1°C
            function("voltage") { batteryInt(BatteryManager.EXTRA_VOLTAGE, 0) }          // mV

            function("isCharging") {
                val plugged = batteryInt(BatteryManager.EXTRA_PLUGGED, 0)
                val status = batteryInt(BatteryManager.EXTRA_STATUS, 0)
                plugged != 0 || status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
            }

            function("batteryInfo") {
                scope.createDynamicObject(
                    mutableMapOf(
                        "level" to batteryInt(BatteryManager.EXTRA_LEVEL, -1),
                        "scale" to batteryInt(BatteryManager.EXTRA_SCALE, 100),
                        "status" to batteryInt(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_UNKNOWN),
                        "health" to batteryInt(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_UNKNOWN),
                        "plugged" to batteryInt(BatteryManager.EXTRA_PLUGGED, 0),
                        "temperature" to batteryInt(BatteryManager.EXTRA_TEMPERATURE, 0),
                        "voltage" to batteryInt(BatteryManager.EXTRA_VOLTAGE, 0),
                        "isCharging" to (batteryInt(BatteryManager.EXTRA_PLUGGED, 0) != 0)
                    )
                )
            }

            function("keepAwake") { args ->
                val on = args.getOrNull(0) as? Boolean ?: true
                val timeout = (args.getOrNull(1) as? Number)?.toLong() ?: 0L
                if (on) {
                    val wl = wakelock ?: pm?.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "CcAuto::keepAwake")
                        ?.apply { setReferenceCounted(false) }
                    wakelock = wl
                    if (timeout > 0) wl?.acquire(timeout) else wl?.acquire()
                    true
                } else {
                    runCatching { wakelock?.takeIf { it.isHeld }?.release() }
                    wakelock = null
                    true
                }
            }

            function("isDeviceIdleMode") { runCatching { pm?.isDeviceIdleMode ?: false }.getOrDefault(false) }
            function("isIgnoringBatteryOptimizations") {
                val ctx = AppRuntime.context ?: return@function false
                runCatching { pm?.isIgnoringBatteryOptimizations(ctx.packageName) ?: false }.getOrDefault(false)
            }

            // 常量
            property("STATUS_UNKNOWN", BatteryManager.BATTERY_STATUS_UNKNOWN)
            property("STATUS_CHARGING", BatteryManager.BATTERY_STATUS_CHARGING)
            property("STATUS_DISCHARGING", BatteryManager.BATTERY_STATUS_DISCHARGING)
            property("STATUS_NOT_CHARGING", BatteryManager.BATTERY_STATUS_NOT_CHARGING)
            property("STATUS_FULL", BatteryManager.BATTERY_STATUS_FULL)
            property("PLUGGED_AC", BatteryManager.BATTERY_PLUGGED_AC)
            property("PLUGGED_USB", BatteryManager.BATTERY_PLUGGED_USB)
            property("PLUGGED_WIRELESS", BatteryManager.BATTERY_PLUGGED_WIRELESS)
        }
    }

    private fun batteryInt(extra: String, def: Int): Int {
        val ctx = AppRuntime.context ?: return def
        val intent: Intent = runCatching {
            ctx.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        }.getOrNull() ?: return def
        return intent.getIntExtra(extra, def)
    }
}
