package com.chouten.app.data

import android.content.Context
import android.util.Base64
import android.webkit.WebView
import android.webkit.WebViewClient
import com.chouten.app.client
import kotlinx.coroutines.sync.Mutex

class WebviewHandler() {
    private lateinit var webview: WebView

    /**
     * Mutex for the webview.
     * This is used to prevent multiple pieces of async code
     * from trying to use the webview at the same time.
     */
    private var injectionLock: Mutex = Mutex(false)

    /**
     * The nextUrl is used to store the url which the webview
     * will navigate to after the current request has been completed.
     * It's used as a fallback and can be changed
     * from within the JS
     */
    private var nextUrl: String = ""

    /**
     * Initialize the webview handler.
     * This should be called before any other webview methods.
     */
    fun initialize(context: Context) {
        if (this::webview.isInitialized) return
        webview = WebView(context)
        webview.settings.javaScriptEnabled = true
    }

    /**
     * Load the webview with data from the given url
     */
    suspend fun load(request: ModuleModel.ModuleCode.ModuleCodeblock.ModuleRequest) {
        // Use the client to get the data from the url
        // and then inject it into the webview.
        val headers = request.headers.mapNotNull {
            if (it.key.isBlank() || it.value.isBlank()) return@mapNotNull null
            it.key to it.value
        }.toMap()

        val url = request.url.isNotBlank().let {
            if (it) request.url else nextUrl
        }

        val data = when (request.type) {
            "GET" -> client.get(url, headers)
            "POST" -> client.post(url, headers, request.body)
            "PUT" -> client.put(url, headers, request.body)
            "DELETE" -> client.delete(url, headers)
            else -> throw Exception("Invalid request type ${request.type}")
        }.body.bytes()

        // The webview expects a Base64 encoded string
        // This allows us to inject data which may not be
        // within UTF-8 range.
        val encodedData = Base64.encodeToString(data, Base64.DEFAULT)
        webview.loadData(encodedData, "text/html", "base64")
        injectionLock.lock()

        // We want the DOM to be loaded before we inject
        // the data into the webview.
        webview.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                // Inject the data into the webview
                // and then remove the webview client.
                // Add the chouten div to the DOM
                webview.evaluateJavascript(
                    """
                    var chouten = document.createElement("div");
                    chouten.id = "chouten";
                    document.body.prepend(chouten);
                    """.trimIndent()
                ) {}
                injectionLock.unlock()

            }
        }

    }

    /**
     * Inject the given javascript into the webview
     */
    suspend fun inject(javascript: ModuleModel.ModuleCode.ModuleCodeblock.ModuleJavascriptOpts): String {
        var results = ""
        // If there's already an injection in progress
        // then we need to wait until it's finished.
        injectionLock.lock()

        if (javascript.removeScripts) {
            // Remove all scripts from the DOM
            webview.evaluateJavascript(
                """
                document.querySelectorAll("script").forEach((el) => {
                    el.remove();
                });
                """.trimIndent()
            ) {}
        }

        // TODO: allowExternalScripts

        // We need to inject the javascript into the webview
        // and then wait for the results to be returned.
        webview.evaluateJavascript(javascript.code) {
            injectionLock.unlock()
        }

        // Each entry in the chouten div
        // is stored inside a <p> tag.
        // We want to make a JSON array of the
        // innerText of each <p> tag.
        val retrieveInjection = """
            var results = [];
            document.querySelectorAll("#chouten > p").forEach((el) => {
                el.innerText = el.innerText.replace(/\\n/g, "");
                results.push(el.innerText);
            });
            results
        """.trimIndent()

        injectionLock.lock()
        webview.evaluateJavascript(retrieveInjection) {
            // it is a JSON Array of strings
            // so we need to parse it.
            results = it ?: "[{}]"
            injectionLock.unlock()
        }

        // Wait for the mutex to be unlocked
        // and then return the results.
        injectionLock.lock()
        injectionLock.unlock()
        return results
    }

    fun updateNextUrl(url: String?) {
        nextUrl = url ?: nextUrl
    }
}