package org.substitute.schedule.update

import kotlinx.coroutines.flow.Flow

expect class PlatformUpdateManager() : UpdateManager{
    override suspend fun checkForUpdate(): UpdateInfo?
    override fun downloadAndInstall(updateInfo: UpdateInfo): Flow<DownloadStatus>
    override fun openStoreOrDownloadPage(updateInfo: UpdateInfo)
    override fun supportsInAppInstallation(): Boolean

}
