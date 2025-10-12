package org.substitute.schedule.update

expect object PlatformContext {
    fun get(): Any // Returns Context on Android, Unit on others
}