package com.chouten.app.ui.views.playground

import android.annotation.SuppressLint
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.chouten.app.PrimaryDataLayer
import com.chouten.app.data.SnackbarVisualsWithError
import com.google.accompanist.web.AccompanistWebViewClient
import com.google.accompanist.web.WebView
import com.google.accompanist.web.rememberWebViewState

class BrowserWebViewClient : AccompanistWebViewClient() {
    private val videoRegex =
        "\\.(mp4|mp4v|mpv|m1v|m4v|mpg|mpg2|mpeg|xvid|webm|3gp|avi|mov|mkv|ogg|ogv|ogm|m3u8|mpd|ism(?:[vc]|/manifest)?)(?:[?#]|$)".toRegex(
            RegexOption.IGNORE_CASE
        )
    private val subtitleRegex =
        "\\.(srt|vtt|ass|ttml|dfxp|xml)(?:[?#]|\$)".toRegex(RegexOption.IGNORE_CASE)

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

    private fun getSubtitleMimeType(uri: String): String? {
        if (uri.isEmpty()) return null

        val matcher = subtitleRegex.find(uri)

        return when (matcher?.groupValues?.getOrNull(1)) {
            "srt" -> "application/srt"//MimeTypes.APPLICATION_SUBRIP
            "vtt" -> "text/vtt"//MimeTypes.TEXT_VTT         !!!!Warning!!!! this format is sometimes used for things other than subtitles
            "ass" -> "text/ssa"//MimeTypes.TEXT_SSA
            "ttml", "dfxp", "xml" -> "application/ttml"//MimeTypes.APPLICATION_TTML
            else -> null
        }
    }

    private fun processURL(uri: String, view: WebView?) {
        //check if video
        val videoMimeType = getVideoMimeType(uri)
        if (videoMimeType != null) {
            // TODO: add url to list or smth here
            Log.d("WebViewClient", "Found video: $uri\nMime type: $videoMimeType")
            PrimaryDataLayer.enqueueSnackbar(
                SnackbarVisualsWithError(
                    "Found Video URL: $uri", false
                )
            )
        }
        //check if subtitle
        val subMimeType = getSubtitleMimeType(uri)
        if (subMimeType != null) {
            // TODO: add url to list or smth here
            Log.d("WebViewClient", "Found subtitle: $uri\nMime type: $subMimeType")
            PrimaryDataLayer.enqueueSnackbar(
                SnackbarVisualsWithError(
                    "Found Subtitle URL: $uri", false
                )
            )
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
    var url = "https://google.com/"
    val state = rememberWebViewState(url = url)
    Column {
        OutlinedTextField(
            value = url,
            onValueChange = {
                url = it
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            label = { Text(text = "URL") }
        )
        WebView(
            state = state,
            captureBackPresses = true,
            modifier = Modifier.fillMaxSize(),
            client = BrowserWebViewClient(),
            onCreated = {
                it.settings.javaScriptEnabled = true
                it.settings.mediaPlaybackRequiresUserGesture = true //TODO: make preference
                it.settings.domStorageEnabled = true
                it.settings.builtInZoomControls = true
                it.settings.displayZoomControls = false
            }
        )
    }
}