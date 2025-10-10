package org.substitute.schedule

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import io.ktor.client.engine.okhttp.OkHttp
import org.substitute.schedule.networking.DsbApiClient
import org.substitute.schedule.networking.createHttpClient

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            App(
                client = DsbApiClient(createHttpClient(OkHttp.create()))

            )
        }
    }
}

@Preview(showSystemUi = true, showBackground = false)
@Composable
fun AppAndroidPreview() {
    App(
        client = DsbApiClient(createHttpClient(OkHttp.create()))
    )
}