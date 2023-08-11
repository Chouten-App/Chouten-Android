package com.chouten.app.data

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.util.Base64
import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebMessage
import android.webkit.WebView
import android.webkit.WebViewClient
import com.chouten.app.LogLayer
import com.chouten.app.Mapper
import com.chouten.app.ModuleLayer
import com.chouten.app.client
import com.chouten.app.preferenceHandler
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext
import kotlinx.serialization.*
import kotlinx.serialization.Serializable
import org.json.JSONObject
import org.json.JSONStringer
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.MediaType
import okhttp3.RequestBody.Companion.toRequestBody;


@Serializable
data class RequestOption(
    val action: String,
    val reqId: String,
    val url: String?,
    val method: String?,
    val body: String?,
    var shouldExit: Boolean?,
    val headers: Map<String, String>?,
    val result: String?
)

class WebviewHandler {
    private var commonCode = """
        let reqId = 0;
        let resolveFunctions = {};

        window.onmessage = async function (event) {
            const data = JSON.parse(event.data);
            let payload = {};

            try{
                payload = JSON.parse(data.payload);
            }catch(err){
                payload = data.payload;
            }

            if (data.action === "logic"){
                try{
                    if(payload.action === "eplist"){
                        await getEpList(payload);
                    }else if(payload.action === "video"){
                        await getSource(payload);
                    }else{
                        await logic(payload);
                    }
                }catch(err){
                    console.error(err);
                    sendSignal(1, err.toString());
                }
            }else{
                resolveFunctions[data.reqId](data.responseText);
            }
        }

        function sendRequest(url, headers, method, body) {
            return new Promise((resolve, reject) => {
                const currentReqId = (++reqId).toString();

                resolveFunctions[currentReqId] = resolve;

                // @ts-ignore
                Native.sendHTTPRequest(JSON.stringify({
                    reqId: currentReqId,
                    action: "HTTPRequest",
                    url,
                    headers,
                    method: method,
                    body: body
                }));
            });
        }

        function sendResult(result, last = false) {
            const currentReqId = (++reqId).toString();

            // @ts-ignore
            Native.sendHTTPRequest(JSON.stringify({
                reqId: currentReqId,
                action: "result",
                shouldExit: last,
                result: JSON.stringify(result)
            }));
        }

        function sendSignal(signal, message = ""){
            const currentReqId = (++reqId).toString();

            // @ts-ignore
            Native.sendHTTPRequest(JSON.stringify({
                reqId: currentReqId,
                action: signal === 0 ? "exit" : "error",
                result: message
            }));
        }

        function loadScript(url){
            return new Promise((resolve, reject) => {
                const script = document.createElement('script');
                
                script.src = url;
                script.onload = resolve;
                script.onerror = reject;
        
                document.head.appendChild(script);
            });
        }

        """;
        
    private lateinit var callback: (String) -> Unit;
    
    /**
     * We don't want the webview to close on error when it's the search page
     */
    private var closeOnError = true;

    private lateinit var webview: WebView

    /**
     * Mutex for the webview. This is used to prevent multiple pieces of async code from trying to
     * use the webview at the same time.
     */
    private var injectionLock: Mutex = Mutex(false)

    /**
     * Mutex for the current call. This is used to prevent a piece of JS from being injected
     * multiple times.
     */
    private var callLock: Mutex = Mutex(false)

    /**
     * The nextUrl is used to store the url which the webview will navigate to after the current
     * request has been completed. It's used as a fallback and can be changed from within the JS
     */
    private var nextUrl: String = ""

    /** Initialize the webview handler. This should be called before any other webview methods. */
    @SuppressLint("SetJavaScriptEnabled", "JavascriptInterface")
    fun initialize(context: Context) {
        if (this::webview.isInitialized) return

        WebView.setWebContentsDebuggingEnabled(true)
        webview = WebView(context)
        webview.settings.javaScriptEnabled = true
        webview.settings.domStorageEnabled = true
        webview.addJavascriptInterface(this, "Native")
    }

    fun setCallback(callback: (String) -> (Unit)){
        this.callback = callback;
    }

    fun dontCloseOnError(){
        this.closeOnError = false;
    }
    
    @JavascriptInterface
    fun sendHTTPRequest(data: String) {
        val self = this

        runBlocking {
            try{ 
                self.postMessage(data) 
            }catch(error: Exception){
                val message = error.message;

                if(message != null){
                    val errorJSON = mapOf<String, String>(
                        "result" to (message),
                        "action" to "error"
                    );
                    self.callback(JSONObject(errorJSON).toString());
                }else{
                    self.callback("{'action': 'error', 'message': 'Error'}");
                }
            }
        }
    }

    suspend fun postMessage(message: String) {
        val req = Mapper.parse<RequestOption>(message);
        var self = this;

        if(req.action == "HTTPRequest" && req.url != null && req.headers != null){
            
            var responseText = "";

            if(req.method == "POST"){
                responseText = client.post(url=req.url, headers=req.headers, requestBody=req.body?.toRequestBody()).body.string();
            }else{
                responseText = client.get(req.url, req.headers).body.string();
            }

            val myWebView = this.webview;
            val response: Map<String, String> = mapOf(
                "reqId" to req.reqId,
                "responseText" to responseText
            );
            
            withContext(Dispatchers.Main) {
                myWebView.postWebMessage(WebMessage(JSONObject(response).toString()), Uri.parse("*"))
            }

        }else if(req.action == "result" && req.result != null){
            this.callback(req.result)
        }else if(req.action == "error"){
            withContext(Dispatchers.Main) {
                self.destroy();
            }

            throw Exception(req.result);
        }else{
            throw Exception("Action not found.");
        }

        if(req.shouldExit == true){
            withContext(Dispatchers.Main) {
                self.destroy();
            }
        }
    }

    @JavascriptInterface
    fun log(message: String) {
        Log.d("WebviewHandler", message)
    }

    /** destroy the webview. And reinstantiate it. */
    fun reset(context: Context) {
        if(this.closeOnError == false){
            return;
        }
        
        if(!preferenceHandler.isDevMode){
            webview.destroy()
            initialize(context)
        }
    }

    fun destroy() {
        if(this.closeOnError == false){
            return;
        }

        if(!preferenceHandler.isDevMode){
            webview.clearCache(true)
            webview.destroy()
        }
    }

    /** Load the webview with data from the given url */
    suspend fun load(code: String, payload: String = ""): Boolean {

        webview.webViewClient = object : WebViewClient() {
            /** We need to call the main function 'logic' only when the code
             *  has been injected
             */
            override fun onPageFinished(view: WebView?, url: String?) {
                val response: Map<String, String> = mapOf(
                    "reqId" to "-1",
                    "action" to "logic",
                    "payload" to payload
                );
    
                webview.postWebMessage(WebMessage(JSONObject(response).toString()), Uri.parse("*"))
            }
        }

        webview.loadDataWithBaseURL(null, "<script>" + this.commonCode + code + "</script>", "text/html; charset=utf-8", "br", null)
        return true
    }
}
