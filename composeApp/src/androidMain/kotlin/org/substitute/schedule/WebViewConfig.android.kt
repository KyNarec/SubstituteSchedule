package org.substitute.schedule

import android.webkit.WebView

actual fun configureWebViewZoom(webView: Any) {
    if (webView is WebView) {
        webView.settings.apply {
            useWideViewPort = true
            loadWithOverviewMode = true
            setSupportZoom(true)
            builtInZoomControls = true
            displayZoomControls = false
        }
        webView.setInitialScale(110) // Adjust this value (30-100)
    }
}