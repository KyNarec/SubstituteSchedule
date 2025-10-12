package org.substitute.schedule.update

import android.content.Context

actual object PlatformContext {
    private var appContext: Context? = null

    fun initialize(context: Context) {
        appContext = context.applicationContext
    }

    actual fun get(): Any = appContext ?: throw IllegalStateException(
        "PlatformContext not initialized. Call PlatformContext.initialize(context) in your Application class."
    )
}