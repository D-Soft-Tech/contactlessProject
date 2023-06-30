package com.woleapp.netpos.contactless.app.loggers

import timber.log.Timber

class ReleaseTree : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        // Disable logging in release builds
    }
}
