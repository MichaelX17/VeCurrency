package com.example.vecurrency.data.remote

import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

object NetworkClient {
    val sharedClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .followRedirects(true)
            .followSslRedirects(true)
            .build()
    }
}
