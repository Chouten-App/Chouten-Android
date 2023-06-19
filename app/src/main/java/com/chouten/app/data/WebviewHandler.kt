package com.chouten.app.data

import android.annotation.SuppressLint
import android.content.Context
import android.util.Base64
import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import com.chouten.app.LogLayer
import com.chouten.app.ModuleLayer
import com.chouten.app.client
import kotlinx.coroutines.sync.Mutex
import java.net.URLDecoder
import java.nio.charset.StandardCharsets


class WebviewHandler {
    private lateinit var webview: WebView

    /**
     * Mutex for the webview.
     * This is used to prevent multiple pieces of async code
     * from trying to use the webview at the same time.
     */
    private var injectionLock: Mutex = Mutex(false)

    /**
     * Mutex for the current call.
     * This is used to prevent a piece of JS
     * from being injected multiple times.
     */
    private var callLock: Mutex = Mutex(false)

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
    @SuppressLint("SetJavaScriptEnabled", "JavascriptInterface")
    fun initialize(context: Context) {
        if (this::webview.isInitialized) return
        webview = WebView(context)
        webview.settings.javaScriptEnabled = true
        webview.settings.domStorageEnabled = true
        webview.addJavascriptInterface(this, "Android")
    }

    /**
     * destroy the webview. And reinstantiate it.
     */
    fun reset(context: Context) {
        webview.destroy()
        initialize(context)
    }

    fun destroy() {
        webview.clearCache(true)
        webview.destroy()
    }

    /**
     * Load the webview with data from the given url
     */
    suspend fun load(codeblock: ModuleModel.ModuleCode.ModuleCodeblock): Boolean {
        var request = codeblock.request

        // Use the client to get the data from the url
        // and then inject it into the webview.
        var headers = request?.headers?.mapNotNull {
            if (it.key.isBlank() || it.value.isBlank()) return@mapNotNull null
            it.key to it.value
        }?.toMap()

        val url = let {
            val url = if (request?.url?.isEmpty() == true) nextUrl else request?.url ?: nextUrl
            if (request == null || request?.url?.isEmpty() == true) request =
                ModuleModel.ModuleCode.ModuleCodeblock.ModuleRequest(url, "GET", listOf(), null)
            url
        }
        println("Requesting $url & Uses api: ${codeblock.usesApi}")
        val data: ByteArray = try {
            var counter = 0
            var responseCode = -1
            lateinit var response: ByteArray
            while (counter < 2 || responseCode != 200) {
                if (headers == null) headers = mapOf("User-Agent" to "Chouten")
                val _response = when (request?.method) {
                    "GET" -> client.get(url, headers)
                    "POST" -> client.post(url, headers, request?.body)
                    "PUT" -> client.put(url, headers, request?.body)
                    "DELETE" -> client.delete(url, headers)
                    else -> client.get(url, headers)
                }
                counter += 1
                responseCode = _response.code
                response = _response.body.bytes()
                if (_response.isSuccessful && responseCode == 200) break
            }
            response
        } catch (e: Exception) {
            LogLayer.addLogEntry(
                LogEntry(
                    title = "Failed to load data from ${request?.url}",
                    message = e.stackTraceToString(),
                    isError = true
                )
            )


            e.printStackTrace()
            return false
        }


        LogLayer.addLogEntry(
            LogEntry(
                title = "Loading data from ${request?.url}",
                message = "Request Type: ${request?.method}\nUses API? ${codeblock.usesApi}",
                isError = false
            )
        )


        // The webview expects a Base64 encoded string
        val encodedData = Base64.encodeToString(data, Base64.DEFAULT)
        val decodedData = String(Base64.decode(encodedData, Base64.DEFAULT))

        if (!codeblock.usesApi!!) {
            webview.loadDataWithBaseURL(null, decodedData, "text/html; charset=utf-8", "br", null)
        } else {
            // We want to load a skeleton
            // so that the webview is ready to go
            // when we inject the data.

            val sanitised =
                decodedData
                    // Replace the HTML ampersand values
                    // e.g &#39; -> '
                    .replace(Regex("&#\\d+;"), "")
                    .replace("'", "")
                    .replace('"', '\'')

            // We want to buffer the sanitised data
            // and print it to the logcat
            // so that we can debug it.
            val chunkSize = 1000
            val chunks = sanitised.chunked(chunkSize)
            chunks.forEachIndexed { index, chunk ->
                Log.d("WebviewHandler", chunk)
            }

            Log.d("WebviewHandler", "Injecting: $sanitised")
            val skeleton = """
                <html>
                    <head>
                    ${
                codeblock.imports?.joinToString("") {
                    val url = URLDecoder.decode(it, "UTF-8")
                    "<script src=\"$url\"></script>"
                } ?: ""
            }
                    </head>
                    <body>
                        <div id="chouten"></div>
                        <div id="json-result" data-json="$sanitised"></div>
                    </body>
                </html>
            """.trimIndent()
            val skeletonEncoded = Base64.encodeToString(
                skeleton.toByteArray(StandardCharsets.UTF_8),
                Base64.DEFAULT
            ).toString()
            webview.loadData(skeletonEncoded, "text/html", "base64")
        }

        injectionLock.lock()

        // We want the DOM to be loaded before we inject
        // the data into the webview.
        webview.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                // Inject the data into the webview
                // and then remove the webview client.
                // Add the chouten div to the DOM
                if (codeblock.usesApi) {
                    injectionLock.unlock()
                    return
                }

                webview.evaluateJavascript(
                    """
                    var chouten = document.createElement("div");
                    chouten.id = "chouten";
                    document.body.prepend(chouten);
                    """.trimIndent()
                ) {
                    injectionLock.unlock()
                }
            }
        }

        webview.webChromeClient = object : WebChromeClient() {
            override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                val level = consoleMessage?.messageLevel()
                val (logType, isError) = if (level == ConsoleMessage.MessageLevel.ERROR) {
                    Pair("Error", true)
                } else {
                    Pair("Info", false)
                }
                LogLayer.addLogEntry(
                    LogEntry(
                        title = logType,
                        module = ModuleLayer.selectedModule
                            ?: throw Exception("No module selected"),
                        message = consoleMessage?.message() ?: "No message",
                        isError = isError,
                    )
                )

                return true
            }
        }

        return true
    }

    /**
     * Inject the given javascript into the webview
     */
    suspend fun inject(
        javascript: ModuleModel.ModuleCode.ModuleCodeblock,
        immediate: Boolean = false
    ): String {
        //println("Running code: ${javascript.code}")
        var results = ""
        // If there's already an injection in progress
        // then we need to wait until it's finished.

        injectionLock.lock()
        if (javascript.removeScripts) {
            callLock.lock()
            // Remove all scripts from the DOM
            webview.evaluateJavascript(
                """
                document.querySelectorAll("script").forEach((el) => {
                    el.remove();
                });
                """.trimIndent()
            ) {
                println("Finished removing scripts")
                callLock.unlock()
            }
        }

        // TODO: allowExternalScripts

        // We need to inject the javascript into the webview
        // and then wait for the results to be returned.
        callLock.lock()
        webview.evaluateJavascript(
            """
            try {
                ${javascript.code}
            } catch (e) {
                console.error(e);
            }
        """.trimIndent()
        ) {
            callLock.unlock()
            // check if immediate is true, and if so, unlock the injection lock and return
            if (immediate) {
                results = it ?: ""
                injectionLock.unlock()
                return@evaluateJavascript
            }

        }

        if (immediate) {
            callLock.lock()
            callLock.unlock()
            injectionLock.lock()
            injectionLock.unlock()
            return results
        }

        // Each entry in the chouten div
        // is stored inside a <p> tag.
        // We want to make a JSON array of the
        // innerText of each <p> tag.
        val retrieveInjection = """
            var results = {result: [], nextUrl: ""};
            // print the html page
            document.querySelectorAll("#chouten > p").forEach((el) => {
                var json = JSON.parse(el.innerText);
                if (Array.isArray(json.result) || Array.isArray(json)) results.result.push(...(Array.isArray(json) ? json : json.result));
                else results.result = json.result ? json.result : [json];
                results.nextUrl = JSON.parse(el.innerText).nextUrl;
            });
            
            if ((results == null || results.result.length == 0) && !results.nextUrl) {
                 var json = JSON.parse(document.getElementById("json-result").getAttribute("data-json"))
                 console.log(document.getElementById("json-result").getAttribute("data-json"));
                 console.log(document.getElementById("json-result"));
                 if (Array.isArray(json.result) || Array.isArray(json)) results.result.push(...(Array.isArray(json) ? json : json.result));
                 else results.result = json.result ? json.result : [json];
                 results.nextUrl = json.nextUrl;
             
            }
       
            // Clear the chouten div
            document.getElementById("chouten").innerHTML = "";
            if (results == null || results.result.length == 0) {
                results = {result: [], nextUrl: results.nextUrl};
            }
            results
        """.trimIndent()

        callLock.lock()
        webview.evaluateJavascript(retrieveInjection) {
            // it is a JSON Array of strings
            // so we need to parse it.
            results = it ?: "{result: [], nextUrl: \"\"}"
            Log.d("WebviewHandler", "Returning $results")
            callLock.unlock()
        }

        // Wait for the mutex to be unlocked
        // and then return the results.
        callLock.lock()
        callLock.unlock()
        injectionLock.unlock()
        return results.trimIndent()
    }

    fun updateNextUrl(url: String?) {
        if (url?.isNotBlank() == true) {
            LogLayer.addLogEntry(
                LogEntry(
                    title = "Setting NextURL to $url",
                    message = "Previous NextURL: $nextUrl",
                    isError = false
                )
            )
        }

        println("Setting NextURL to $url")
        nextUrl = url ?: nextUrl
    }
}