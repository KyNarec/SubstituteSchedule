package org.substitute.schedule.update

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException

actual class PlatformUpdateManager : UpdateManager {
    private val client = OkHttpClient()

    actual override suspend fun checkForUpdate(): UpdateInfo? = withContext(Dispatchers.IO) {
        // Similar to Android - fetch from GitHub
        val request = Request.Builder()
            .url("https://api.github.com/KyNarec/SubstituteSchedule/releases/latest")
            .build()

        val json = Json { ignoreUnknownKeys = true }

        val response = client.newCall(request).execute()
        val release = json.decodeFromString<GitHubRelease>(response.body.string())

        val currentVersion = Version.parse(getCurrentVersion()) ?: return@withContext null
        val latestVersion = Version.parse(release.tagName?.removePrefix("v")?: "") ?: return@withContext null

        if (latestVersion > currentVersion) {
            // Find appropriate installer (dmg for macOS, exe for Windows, deb/AppImage for Linux)
            val os = System.getProperty("os.name").lowercase()
            val asset = release.assets.find { asset ->
                when {
                    "mac" in os -> asset.name.endsWith(".dmg")
                    "windows" in os -> asset.name.endsWith(".exe") || asset.name.endsWith(".msi")
                    else -> asset.name.endsWith(".deb") || asset.name.endsWith(".AppImage")
                }
            }

            UpdateInfo(
                version = release.tagName?: "",
                releaseNotes = release.body,
                downloadUrl = asset?.browserDownloadUrl,
                storeUrl = "https://github.com/KyNarec/SubstituteSchedule/releases/latest",
                releaseDate = release.publishedAt?: ""
            )
        } else null
    }

    actual override fun downloadAndInstall(updateInfo: UpdateInfo): Flow<DownloadStatus> = flow {
        val downloadUrl = updateInfo.downloadUrl ?: throw IllegalStateException("No download URL")

        emit(DownloadStatus.Progress(0, 0, 0))

        val request = Request.Builder().url(downloadUrl).build()
        val response = client.newCall(request).execute()
        val body = response.body ?: throw IOException("Empty response body") as Throwable

        val downloadsDir = File(System.getProperty("user.home"), "Downloads")
        val fileName = downloadUrl.substringAfterLast("/")
        val file = File(downloadsDir, fileName)

        val totalBytes = body.contentLength()
        var downloadedBytes = 0L

        body.byteStream().use { input ->
            file.outputStream().use { output ->
                val buffer = ByteArray(8192)
                var bytes = input.read(buffer)

                while (bytes >= 0) {
                    output.write(buffer, 0, bytes)
                    downloadedBytes += bytes

                    val percent = ((downloadedBytes * 100) / totalBytes).toInt()
                    emit(DownloadStatus.Progress(percent, downloadedBytes, totalBytes))

                    bytes = input.read(buffer)
                }
            }
        }

        emit(DownloadStatus.Completed(file.absolutePath))

        // Open the downloaded file with default application
        java.awt.Desktop.getDesktop().open(file)
    }.flowOn(Dispatchers.IO)

    actual override fun openStoreOrDownloadPage(updateInfo: UpdateInfo) {
        val uri = java.net.URI(updateInfo.storeUrl ?: "https://github.com/KyNarec/SubstituteSchedule")
        java.awt.Desktop.getDesktop().browse(uri)
    }

    actual override fun supportsInAppInstallation(): Boolean = true
}