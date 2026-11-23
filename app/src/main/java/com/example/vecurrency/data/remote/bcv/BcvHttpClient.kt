package com.example.vecurrency.data.remote.bcv

import com.example.vecurrency.data.remote.NetworkClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

open class BcvHttpClient(
    private val client: OkHttpClient = NetworkClient.sharedClient,
    private val baseUrl: String = BCV_URL
) {
    companion object {
        const val BCV_URL = "https://www.bcv.org.ve/"
        private const val USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
    }

    open suspend fun fetchBcvHtml(): String = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(baseUrl)
            .header("User-Agent", USER_AGENT)
            .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            .header("Accept-Language", "es-ES,es;q=0.9,en;q=0.8")
            .get()
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("BCV server returned HTTP status ${response.code}: ${response.message}")
            }
            val body = response.body
                ?: throw IOException("BCV server returned empty body")
            body.string()
        }
    }
}
