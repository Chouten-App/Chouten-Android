package com.chouten.app

import android.content.Context
import android.os.Build
import android.util.Log
import com.chouten.app.data.CustomDNS
import dev.brahmkshatriya.nicehttp.Requests
import dev.brahmkshatriya.nicehttp.ResponseParser
import dev.brahmkshatriya.nicehttp.addGenericDns
import kotlinx.coroutines.*
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import okhttp3.Cache
import okhttp3.OkHttpClient
import java.io.File
import java.io.PrintWriter
import java.io.Serializable
import java.io.StringWriter
import java.util.concurrent.*
import kotlin.reflect.KClass
import kotlin.reflect.KFunction

// Borrowed from https://github.com/saikou-app/saikou/blob/main/app/src/main/java/ani/saikou/Network.kt
//TODO: properly implement functions, this is a half-assed copy

val defaultHeaders = mapOf(
    "User-Agent" to
            "Mozilla/5.0 (Linux; Android %s; %s) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.4896.127 Mobile Safari/537.36"
                .format(Build.VERSION.RELEASE, Build.MODEL)
)
lateinit var cache: Cache

lateinit var okHttpClient: OkHttpClient
lateinit var client: Requests

fun initializeNetwork(context: Context) {
    val dns = preferenceHandler.dns
    cache = Cache(
        File(context.cacheDir, "http_cache"),
        5 * 1024L * 1024L // 5 MiB
    )
    okHttpClient = OkHttpClient.Builder()
        .followRedirects(true)
        .followSslRedirects(true)
        .cache(cache)
        .apply {
            when (dns) {
                CustomDNS.GOOGLE -> addGoogleDns()
                CustomDNS.CLOUDFLARE -> addCloudFlareDns()
                CustomDNS.ADGUARD -> addAdGuardDns()
                else -> {}
            }
        }
        .build()
    client = Requests(
        okHttpClient,
        defaultHeaders,
        defaultCacheTime = 6,
        defaultCacheTimeUnit = TimeUnit.HOURS,
        responseParser = Mapper
    )
}

object Mapper : ResponseParser {

    @OptIn(ExperimentalSerializationApi::class)
    val json = Json {
        isLenient = true
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    @OptIn(InternalSerializationApi::class)
    override fun <T : Any> parse(text: String, kClass: KClass<T>): T {
        return json.decodeFromString(kClass.serializer(), text)
    }

    override fun <T : Any> parseSafe(text: String, kClass: KClass<T>): T? {
        return try {
            parse(text, kClass)
        } catch (e: Exception) {
            null
        }
    }

    inline fun <reified T> parse(text: String): T {
        return json.decodeFromString(text)
    }
}

fun <A, B> Collection<A>.asyncMap(f: suspend (A) -> B): List<B> = runBlocking {
    map { async { f(it) } }.map { it.await() }
}

fun <A, B> Collection<A>.asyncMapNotNull(f: suspend (A) -> B?): List<B> =
    runBlocking {
        map { async { f(it) } }.mapNotNull { it.await() }
    }

fun logError(e: Exception, post: Boolean = true/*, snackbar: Boolean = true*/) {
    val sw = StringWriter()
    val pw = PrintWriter(sw)
    e.printStackTrace(pw)
    val stackTrace: String = sw.toString()
    if (post) {
        //if (snackbar)
        //snackString(e.localizedMessage, null, stackTrace)
        //else
        //toast(e.localizedMessage)
        Log.d("Error", stackTrace)
    }
    e.printStackTrace()
}

fun <T> tryWith(
    post: Boolean = false/*, snackbar: Boolean = true*/,
    call: () -> T
): T? {
    return try {
        call.invoke()
    } catch (e: Exception) {
        logError(e, post/*, snackbar*/)
        null
    }
}

suspend fun <T> tryWithSuspend(
    post: Boolean = false/*, snackbar: Boolean = true*/,
    call: suspend () -> T
): T? {
    return try {
        call.invoke()
    } catch (e: Exception) {
        logError(e, post/*, snackbar*/)
        null
    } catch (e: CancellationException) {
        null
    }
}

/**
 * A url, which can also have headers
 * **/
data class FileUrl(
    val url: String,
    val headers: Map<String, String> = mapOf()
) : Serializable {
    companion object {
        operator fun get(
            url: String?,
            headers: Map<String, String> = mapOf()
        ): FileUrl? {
            return FileUrl(url ?: return null, headers)
        }
    }
}

//Credits to leg
data class Lazier<T>(
    val lClass: KFunction<T>,
    val name: String
) {
    val get = lazy { lClass.call() }
}

fun <T> lazyList(vararg objects: Pair<String, KFunction<T>>): List<Lazier<T>> {
    return objects.map {
        Lazier(it.second, it.first)
    }
}

fun <T> T.printIt(pre: String = ""): T {
    println("$pre$this")
    return this
}


fun OkHttpClient.Builder.addGoogleDns() = (
        addGenericDns(
            "https://dns.google/dns-query",
            listOf(
                "8.8.4.4",
                "8.8.8.8"
            )
        ))

fun OkHttpClient.Builder.addCloudFlareDns() = (
        addGenericDns(
            "https://cloudflare-dns.com/dns-query",
            listOf(
                "1.1.1.1",
                "1.0.0.1",
                "2606:4700:4700::1111",
                "2606:4700:4700::1001"
            )
        ))

fun OkHttpClient.Builder.addAdGuardDns() = (
        addGenericDns(
            "https://dns.adguard.com/dns-query",
            listOf(
                // "Non-filtering"
                "94.140.14.140",
                "94.140.14.141",
            )
        ))
