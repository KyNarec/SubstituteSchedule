package org.substitute.schedule.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.content.FileProvider
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

actual class PlatformUpdateManager actual constructor() : UpdateManager {
    private val client = OkHttpClient()
    private val context: Context
        get() = PlatformContext.get() as Context

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    actual override suspend fun checkForUpdate(): UpdateInfo? = withContext(Dispatchers.IO) {
        try {
            Log.i("PlatformUpdateManager", "Checking for updates...")

            val request = Request.Builder()
                .url("https://api.github.com/repos/KyNarec/SubstituteSchedule/releases/latest")
                .build()

            val response = client.newCall(request).execute()
            Log.i("PlatformUpdateManager", "Response code: ${response.code}")

            val responseBody = response.body?.string()
                ?: throw IOException("Empty response body")
            Log.i("PlatformUpdateManager", "Response body length: ${responseBody.length}")

            val release = json.decodeFromString<GitHubRelease>(responseBody)
            Log.i("PlatformUpdateManager", "Release parsed: ${release.tagName}")

            val currentVersionString = getCurrentVersion()
            Log.i("PlatformUpdateManager", "Current version string: $currentVersionString")

            val currentVersion = Version.parse(currentVersionString)
            Log.i("PlatformUpdateManager", "Current version parsed: $currentVersion")

            if (currentVersion == null) {
                Log.e("PlatformUpdateManager", "Failed to parse current version")
                return@withContext null
            }

            val latestVersion = Version.parse(release.tagName.removePrefix("v"))
            Log.i("PlatformUpdateManager", "Latest version parsed: $latestVersion")

            if (latestVersion == null) {
                Log.e("PlatformUpdateManager", "Failed to parse latest version")
                return@withContext null
            }

            Log.i("PlatformUpdateManager", "Current version: $currentVersion, Latest version: $latestVersion")

            if (latestVersion > currentVersion) {
                // Log all available assets
                Log.i("PlatformUpdateManager", "Available assets:")
                release.assets.forEach { asset ->
                    Log.i("PlatformUpdateManager", "  - ${asset.name}")
                }
                Log.i("PlatformUpdateManager", "Device ABIs: ${Build.SUPPORTED_ABIS.joinToString()}")

                // Try to find APK matching device ABI
                var apkAsset = release.assets.find {
                    it.name.endsWith(".apk") && it.name.contains(Build.SUPPORTED_ABIS[0])
                }

                // Fallback: try any supported ABI
                if (apkAsset == null) {
                    apkAsset = release.assets.find { asset ->
                        asset.name.endsWith(".apk") && Build.SUPPORTED_ABIS.any { abi ->
                            asset.name.contains(abi, ignoreCase = true)
                        }
                    }
                }

                // Last resort: just grab any APK (universal build)
                if (apkAsset == null) {
                    apkAsset = release.assets.firstOrNull { it.name.endsWith(".apk") }
                }

                Log.i("PlatformUpdateManager", "Selected APK: ${apkAsset?.name}")

                if (apkAsset == null) {
                    Log.e("PlatformUpdateManager", "No APK found in release assets!")
                    return@withContext null
                }

                UpdateInfo(
                    version = release.tagName,
                    releaseNotes = release.body,
                    downloadUrl = apkAsset.browserDownloadUrl,
                    storeUrl = "https://play.google.com/store/apps/details?id=${context.packageName}",
                    releaseDate = release.publishedAt
                )
            } else {
                Log.i("PlatformUpdateManager", "No update available")
                null
            }
        } catch (e: Exception) {
            Log.e("PlatformUpdateManager", "Error checking for updates", e)
            e.printStackTrace()
            null
        }
    }

    actual override fun downloadAndInstall(updateInfo: UpdateInfo): Flow<DownloadStatus> = flow {
        val downloadUrl = updateInfo.downloadUrl ?: throw IllegalStateException("No download URL")

        emit(DownloadStatus.Progress(0, 0, 0))

        val request = Request.Builder().url(downloadUrl).build()
        val response = client.newCall(request).execute()
        val body = response.body ?: throw IOException("Empty response body")

        val apkFile = File(context.getExternalFilesDir("updates"), "latest.apk")
        apkFile.parentFile?.mkdirs()

        val totalBytes = body.contentLength()
        var downloadedBytes = 0L

        body.byteStream().use { input ->
            apkFile.outputStream().use { output ->
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

        emit(DownloadStatus.Completed(apkFile.absolutePath))

        // Trigger installation
        installApk(apkFile)
    }.flowOn(Dispatchers.IO)

    private fun installApk(file: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(intent)
    }

    actual override fun openStoreOrDownloadPage(updateInfo: UpdateInfo) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(updateInfo.storeUrl))
        context.startActivity(intent)
    }

    actual override fun supportsInAppInstallation(): Boolean = true
}