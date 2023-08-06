package com.chouten.app.ui.views.playground

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.PermissionRequest
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.chouten.app.PrimaryDataLayer
import com.chouten.app.data.SnackbarVisualsWithError
import com.google.accompanist.web.AccompanistWebChromeClient
import com.google.accompanist.web.AccompanistWebViewClient
import com.google.accompanist.web.WebView
import com.google.accompanist.web.rememberWebViewState

class BrowserWebViewClient(private val callback: (String) -> Unit) : AccompanistWebViewClient() {
    private val videoRegex =
        "\\.(mp4|mp4v|mpv|m1v|m4v|mpg|mpg2|mpeg|xvid|webm|3gp|avi|mov|mkv|ogg|ogv|ogm|m3u8|mpd|ism(?:[vc]|/manifest)?)(?:[?#]|$)".toRegex(
            RegexOption.IGNORE_CASE
        )
    private val subtitleRegex =
        "\\.(srt|vtt|ass|ttml|dfxp|xml)(?:[?#]|\$)".toRegex(RegexOption.IGNORE_CASE)

    private fun getVideoMimeType(uri: String): String? {
        if (uri.isEmpty()) return null

        return videoRegex.find(uri)?.groupValues?.getOrNull(1)?.let { mimeType ->
            when (mimeType) {
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
    }

    private fun getSubtitleMimeType(uri: String): String? {
        if (uri.isEmpty()) return null

        return subtitleRegex.find(uri)?.groupValues?.getOrNull(1)?.let { mimeType ->
            when (mimeType) {
                "srt" -> "application/srt" //MimeTypes.APPLICATION_SUBRIP
                "vtt" -> "text/vtt" //MimeTypes.TEXT_VTT         !!!!Warning!!!! this format is sometimes used for things other than subtitles
                "ass" -> "text/ssa" //MimeTypes.TEXT_SSA
                "ttml", "dfxp", "xml" -> "application/ttml" //MimeTypes.APPLICATION_TTML
                else -> null
            }
        }
    }

    private fun processURL(uri: String, view: WebView?): Boolean {
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
            return true
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
            return true
        }
        return false
    }

    @Deprecated("Deprecated in Java")
    override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
        return processURL(url, view)
    }

    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        val url = request.url.toString()
        return processURL(url, view)
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

    override fun onPageFinished(view: WebView?, url: String?) {
        if (url != null) {
            callback(url)
        }
    }
}

class BrowserWebChromeClient : AccompanistWebChromeClient() {
    override fun onProgressChanged(view: WebView?, newProgress: Int) {
        super.onProgressChanged(view, newProgress)
        Log.d("WebChromeClient", "Progress: $newProgress")
    }

    override fun onPermissionRequest(request: PermissionRequest) {
        val resources = request.resources
        for (i in resources.indices) {
            if (PermissionRequest.RESOURCE_PROTECTED_MEDIA_ID == resources[i]) {
                request.grant(resources)
                return
            }
        }
        super.onPermissionRequest(request)
    }

    override fun onConsoleMessage(message: ConsoleMessage): Boolean {
        Log.d(
            "Console",
            "${message.message()} -- From line " + "${message.lineNumber()} of ${message.sourceId()}"
        )
        return true
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun PlayGroundPage(
    navController: NavController
) {
    var fieldUrl by remember { mutableStateOf("https://google.com/") }
    val state = rememberWebViewState(url = fieldUrl)

    val handler = Handler(Looper.getMainLooper())
    var previousUrl: String? = null
    var timerId: Runnable? = null

    val updateCurrentUrl: (String) -> Unit = { url ->
        if (url != previousUrl) {
            timerId?.let { handler.removeCallbacks(it) } // Cancel any existing timer

            timerId = Runnable {
                fieldUrl = url
                previousUrl = url
            }

            handler.postDelayed(timerId!!, 1000) // Set a new timer
        }
    }

    Column(modifier = Modifier.statusBarsPadding()) {
        OutlinedTextField(
            value = fieldUrl,
            onValueChange = {
                fieldUrl = it
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
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
            client = BrowserWebViewClient(updateCurrentUrl),
            chromeClient = BrowserWebChromeClient(),
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