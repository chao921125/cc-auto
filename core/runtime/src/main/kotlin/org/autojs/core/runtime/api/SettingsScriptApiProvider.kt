package org.autojs.core.runtime.api

import org.autojs.core.api.ApiProvider
import org.autojs.core.api.ScriptScope
import org.autojs.core.runtime.AppRuntime

/**
 * settings 命名空间：系统设置读写（对齐 AutoJS Pro settings 模块）。
 * 读取 Settings.System/Secure；写入需 WRITE_SETTINGS 授权（未授权时安全返回 false）。
 */
class SettingsScriptApiProvider : ApiProvider {
    override val name = "settings"

    override fun install(scope: ScriptScope) {
        scope.defineObject("settings") {
            function("get") { args ->
                val key = args.getOrNull(0)?.toString() ?: return@function null
                val def = (args.getOrNull(1) as? Number)?.toInt() ?: -1
                val cr = AppRuntime.context?.contentResolver ?: return@function null
                try {
                    android.provider.Settings.System.getInt(cr, key, def)
                } catch (e: Exception) {
                    try {
                        android.provider.Settings.Secure.getInt(cr, key, def)
                    } catch (e2: Exception) { null }
                }
            }
            function("getFloat") { args ->
                val key = args.getOrNull(0)?.toString() ?: return@function null
                val cr = AppRuntime.context?.contentResolver ?: return@function null
                try {
                    android.provider.Settings.System.getFloat(cr, key, -1f)
                } catch (e: Exception) { null }
            }
            function("set") { args ->
                val key = args.getOrNull(0)?.toString() ?: return@function false
                val value = (args.getOrNull(1) as? Number)?.toInt() ?: return@function false
                val ctx = AppRuntime.context ?: return@function false
                try {
                    android.provider.Settings.System.putInt(ctx.contentResolver, key, value)
                    true
                } catch (e: SecurityException) {
                    false
                } catch (e: Exception) {
                    false
                }
            }
            function("canWrite") { _ ->
                AppRuntime.context?.let {
                    android.provider.Settings.System.canWrite(it)
                } ?: false
            }
            function("requestWritePermission") { _ ->
                val ctx = AppRuntime.context ?: return@function false
                android.content.Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS)
                    .setData(android.net.Uri.parse("package:${ctx.packageName}"))
                    .addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                    .let { ctx.startActivity(it) }
                true
            }
        }
    }
}
