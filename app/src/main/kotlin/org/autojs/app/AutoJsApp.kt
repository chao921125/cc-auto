package org.autojs.app

import android.app.Application
import com.tencent.mmkv.MMKV
import org.autojs.app.ui.UiHostImpl
import org.autojs.core.runtime.UiBridge

class AutoJsApp : Application() {
    override fun onCreate() {
        super.onCreate()
        MMKV.initialize(this)
        UiBridge.host = UiHostImpl(this)
    }
}
