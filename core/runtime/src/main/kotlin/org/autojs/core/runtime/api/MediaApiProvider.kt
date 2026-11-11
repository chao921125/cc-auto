package org.autojs.core.runtime.api

import android.media.MediaPlayer
import org.autojs.core.api.ApiProvider
import org.autojs.core.api.ScriptScope
import org.autojs.core.runtime.AppRuntime
import java.io.File

/**
 * media 命名空间：音频播放（对齐 AutoJS Pro media 模块核心能力）。
 */
class MediaApiProvider : ApiProvider {
    override val name = "media"

    @Volatile
    private var player: MediaPlayer? = null

    override fun install(scope: ScriptScope) {
        scope.defineObject("media") {
            function("playMusic") { args ->
                val path = args.getOrNull(0)?.toString() ?: ""
                val loop = args.getOrNull(1) as? Boolean ?: false
                play(path, loop)
            }
            function("stopMusic") { _ -> stop(); null }
            function("pauseMusic") { _ -> runCatching { player?.pause() }; null }
            function("resumeMusic") { _ -> runCatching { player?.start() }; null }
            function("isMusicPlaying") { _ -> runCatching { player?.isPlaying == true }.getOrDefault(false) }
            function("setMusicVolume") { args ->
                val v = (args.getOrNull(0) as? Number)?.toFloat()?.coerceIn(0f, 1f) ?: 1f
                runCatching { player?.setVolume(v, v) }
                null
            }
            function("stopAll") { _ -> stop(); null }
        }
    }

    private fun play(path: String, loop: Boolean): Boolean {
        return try {
            stop()
            val f = File(path)
            if (!f.exists()) return false
            val p = MediaPlayer()
            p.setDataSource(f.absolutePath)
            p.isLooping = loop
            p.prepare()
            p.start()
            player = p
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun stop() {
        runCatching { player?.stop() }
        runCatching { player?.release() }
        player = null
    }

    @Suppress("unused")
    private fun ctx() = AppRuntime.context
}
