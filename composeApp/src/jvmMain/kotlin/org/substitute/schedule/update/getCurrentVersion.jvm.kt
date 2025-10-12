package org.substitute.schedule.update

actual fun getCurrentVersion(): String {
    return try {
        val properties = java.util.Properties()
        val inputStream = object {}.javaClass.getResourceAsStream("/version.properties")

        if (inputStream != null) {
            properties.load(inputStream)
            properties.getProperty("version", "1.0.0")
        } else {
            "1.0.0" // Fallback
        }
    } catch (e: Exception) {
        e.printStackTrace()
        "1.0.0" // Fallback
    }
}