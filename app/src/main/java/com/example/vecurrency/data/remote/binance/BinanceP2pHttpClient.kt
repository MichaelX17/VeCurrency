package com.example.vecurrency.data.remote.binance

import com.example.vecurrency.data.remote.NetworkClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

open class BinanceP2pHttpClient(
    private val client: OkHttpClient = NetworkClient.sharedClient,
    private val dolarApiUrl: String = DOLAR_API_URL,
    private val yadioUrl: String = YADIO_API_URL
) {
    companion object {
        const val DOLAR_API_URL = "https://ve.dolarapi.com/v1/dolares/paralelo"
        const val DOLAR_API_OFFICIAL_USD_URL = "https://ve.dolarapi.com/v1/dolares/oficial"
        const val DOLAR_API_OFFICIAL_EUR_URL = "https://ve.dolarapi.com/v1/euros/oficial"
        const val YADIO_API_URL = "https://api.yadio.io/json"
        private const val USER_AGENT = "VeCurrency/1.0 (Android)"
    }

    open suspend fun fetchDolarApiUsdtRate(): String = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(dolarApiUrl)
            .header("User-Agent", USER_AGENT)
            .header("Accept", "application/json")
            .get()
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("DolarApi returned HTTP status ${response.code}: ${response.message}")
            }
            val responseBody = response.body
                ?: throw IOException("DolarApi returned empty response body")
            responseBody.string()
        }
    }

    open suspend fun fetchDolarApiOfficialUsdRate(): String = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(DOLAR_API_OFFICIAL_USD_URL)
            .header("User-Agent", USER_AGENT)
            .header("Accept", "application/json")
            .get()
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("DolarApi returned HTTP status ${response.code}: ${response.message}")
            }
            val responseBody = response.body
                ?: throw IOException("DolarApi returned empty response body")
            responseBody.string()
        }
    }

    open suspend fun fetchDolarApiOfficialEurRate(): String = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(DOLAR_API_OFFICIAL_EUR_URL)
            .header("User-Agent", USER_AGENT)
            .header("Accept", "application/json")
            .get()
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("DolarApi returned HTTP status ${response.code}: ${response.message}")
            }
            val responseBody = response.body
                ?: throw IOException("DolarApi returned empty response body")
            responseBody.string()
        }
    }

    open suspend fun fetchYadioUsdtRate(): String = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(yadioUrl)
            .header("User-Agent", USER_AGENT)
            .header("Accept", "application/json")
            .get()
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Yadio API returned HTTP status ${response.code}: ${response.message}")
            }
            val responseBody = response.body
                ?: throw IOException("Yadio API returned empty response body")
            responseBody.string()
        }
    }
}
