package org.substitute.schedule.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.multiplatform.webview.web.LoadingState
import com.multiplatform.webview.web.WebView
import com.multiplatform.webview.web.rememberWebViewState
import org.substitute.schedule.configureWebViewZoom

@Composable
fun WebViewScreen(url: String) {
    val state = rememberWebViewState(url)

    LaunchedEffect(Unit) {
        state.webSettings.apply {
            androidWebSettings.apply {
                supportZoom = true
            }
        }
    }

    val loadingState = state.loadingState

    Column(modifier = Modifier.fillMaxSize()) {
        if (loadingState is LoadingState.Loading) {
            LinearProgressIndicator(
                progress = { loadingState.progress },
                modifier = Modifier.fillMaxWidth()
                    .align(Alignment.CenterHorizontally)
            )
        }
        WebView(
            state = state,
            modifier = Modifier.fillMaxSize().padding(0.dp)
                .background(MaterialTheme.colorScheme.background),
//                            navigator = navigator,
            onCreated = { webView ->
                configureWebViewZoom(webView)
            }
        )

    }
}