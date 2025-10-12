package org.substitute.schedule

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import io.ktor.client.engine.okhttp.OkHttp
import okhttp3.internal.platform.PlatformRegistry.applicationContext
import org.substitute.schedule.networking.DsbApiClient
import org.substitute.schedule.networking.createHttpClient
import org.substitute.schedule.update.PlatformContext
import org.substitute.schedule.utils.AndroidSecureStorage

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        PlatformContext.initialize(applicationContext)

        setContent {
            App(
                client = DsbApiClient(createHttpClient(OkHttp.create())),
                storage = remember {
                    AndroidSecureStorage(applicationContext)
                }
            )
        }
    }
}