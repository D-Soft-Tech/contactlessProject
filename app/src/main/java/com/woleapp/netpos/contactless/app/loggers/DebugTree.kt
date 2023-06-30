package com.woleapp.netpos.contactless.app.loggers

import android.util.Log
import com.woleapp.netpos.contactless.BuildConfig
import timber.log.Timber

class DebugTree : Timber.DebugTree() {
    override fun isLoggable(tag: String?, priority: Int): Boolean {
        // Log for all priorities in debug builds
        return BuildConfig.DEBUG || priority >= Log.WARN
    }
}
