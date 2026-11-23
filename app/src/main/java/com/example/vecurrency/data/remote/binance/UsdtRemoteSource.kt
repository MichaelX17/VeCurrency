package com.example.vecurrency.data.remote.binance

import com.example.vecurrency.domain.model.ExchangeRate
import kotlinx.coroutines.CancellationException
import java.time.Instant

class UsdtRemoteSource(
    private val httpClient: BinanceP2pHttpClient = BinanceP2pHttpClient(),
    private val parser: UsdtP2pParser = UsdtP2pParser
) : UsdtSource {

    override suspend fun fetchUsdtRate(): Result<ExchangeRate> {
        // Primary attempt: DolarApi (P2P Parallel Rate for VES)
        try {
            val json = httpClient.fetchDolarApiUsdtRate()
            val response = parser.parseDolarApiResponse(json)
            val rate = parser.computeRateFromDolarApi(response, Instant.now())
            return Result.success(rate)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            // Secondary fallback attempt: Yadio API
            return try {
                val json = httpClient.fetchYadioUsdtRate()
                val response = parser.parseYadioResponse(json)
                val rate = parser.computeRateFromYadio(response, Instant.now())
                Result.success(rate)
            } catch (fallbackEx: Exception) {
                if (fallbackEx is CancellationException) throw fallbackEx
                Result.failure(e)
            }
        }
    }
}
