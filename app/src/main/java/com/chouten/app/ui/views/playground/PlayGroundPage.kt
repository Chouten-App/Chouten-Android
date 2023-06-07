package com.chouten.app.ui.views.playground

import android.annotation.SuppressLint
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController

class BrowserWebViewClient : WebViewClient() {
    private val videoRegex =
        "\\.(mp4|mp4v|mpv|m1v|m4v|mpg|mpg2|mpeg|xvid|webm|3gp|avi|mov|mkv|ogg|ogv|ogm|m3u8|mpd|ism(?:[vc]|/manifest)?)(?:[?#]|$)".toRegex(
            RegexOption.IGNORE_CASE
        )

    private fun getVideoMimeType(uri: String): String? {
        if (uri.isEmpty()) return null

        val matcher = videoRegex.find(uri)

        return when (matcher?.groupValues?.getOrNull(1)) {
            "mp4", "mp4v", "m4v" -> "video/mp4"
            "mpv" -> "video/MPV"
            "m1v", "mpg", "mpg2", "mpeg" -> "video/mpeg"
            "xvid" -> "video/x-xvid"
            "webm" -> "video/webm"
            "3gp" -> "video/3gpp"
            "avi" -> "video/x-msvideo"
            "mov" -> "video/quicktime"
            "mkv" -> "video/x-mkv"
            "ogg", "ogv", "ogm" -> "video/ogg"
            "m3u8" -> "application/x-mpegURL"
            "mpd" -> "application/dash+xml"
            "ism", "ism/manifest", "ismv", "ismc" -> "application/vnd.ms-sstr+xml"
            else -> null
        }
    }


    private fun processURL(uri: String, view: WebView?) {
        Log.d("WebViewClient", "Processing URL: $uri")
        val mimeType = getVideoMimeType(uri)
        if (mimeType != null) {
            // add url to list
            Log.d("WebViewClient", "Found video: $uri\nMime type: $mimeType")
        }
    }

    @Deprecated("Deprecated in Java")
    override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
        processURL(url, view)
        return false
    }

    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        val url = request.url.toString()
        processURL(url, view)
        return false
    }

    @Deprecated("Deprecated in Java")
    override fun shouldInterceptRequest(view: WebView, url: String): WebResourceResponse? {
        processURL(url, view)
        return null
    }

    override fun shouldInterceptRequest(
        view: WebView,
        request: WebResourceRequest
    ): WebResourceResponse? {
        val url = request.url.toString()
        processURL(url, view)
        return null
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun PlayGroundPage(
    navController: NavController
) {
    var url = "https://google.com"
    Column {
        OutlinedTextField(
            value = url,
            onValueChange = {
                url = it
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
        var Browser = BrowserView(url)
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun BrowserView(url: String) {
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.mediaPlaybackRequiresUserGesture = true
                webViewClient = BrowserWebViewClient()
                loadUrl(url)
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}