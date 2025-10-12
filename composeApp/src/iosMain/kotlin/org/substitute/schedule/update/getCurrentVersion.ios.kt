package org.substitute.schedule.update

actual fun getCurrentVersion(): String {
    return platform.Foundation.NSBundle.mainBundle.infoDictionary
        ?.get("CFBundleShortVersionString") as? String ?: "0.0.0"
}