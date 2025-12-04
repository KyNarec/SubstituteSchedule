package org.substitute.schedule

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposableTargetMarker
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.multiplatform.webview.util.addTempDirectoryRemovalHook
import dev.datlag.kcef.KCEF
import dev.datlag.kcef.KCEFBuilder
import io.ktor.client.engine.okhttp.OkHttp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.substitute.schedule.networking.DsbApiClient
import org.substitute.schedule.networking.createHttpClient
import org.substitute.schedule.utils.DesktopSecureStorage
import java.io.File
import kotlin.math.max

fun main() = application {
    addTempDirectoryRemovalHook()
    Window(onCloseRequest = ::exitApplication) {
        var restartRequired by remember { mutableStateOf(false) }
        var downloading by remember { mutableStateOf(0F) }
        var initialized by remember { mutableStateOf(false) }
        val bundleLocation = System.getProperty("compose.application.resources.dir")?.let { File(it) } ?: File(".")
        val download: KCEFBuilder.Download = remember { KCEFBuilder.Download.Builder().github().build() }

        LaunchedEffect(Unit) {
            withContext(Dispatchers.IO) {
                KCEF.init(builder = {
                    installDir(File(bundleLocation, "kcef-bundle"))
                    progress {
                        onDownloading {
                            downloading = it
                        }
                        onInitialized {
                            initialized = true
                        }
                    }
                    download {
                        github {
                            release("jbr-release-17.0.12b1207.37")
                        }
                    }

                    settings {
                        cachePath = File("cache").absolutePath
                    }
                }, onError = {
                    it?.printStackTrace()
                }, onRestartRequired = {
                    restartRequired = true
                })
            }
        }

        if (restartRequired) {
            Text(text = "Restart required.")
        } else {
            if (initialized) {
                App(
                    client = DsbApiClient(createHttpClient(OkHttp.create())),
                    storage = remember {
                        DesktopSecureStorage()
                    }
                )
            } else {
                Text(text = "Downloading $downloading%")
            }
        }

//        DisposableEffect(Unit) {
//            onDispose {
//                KCEF.disposeBlocking()
//            }
//        }
    }
}