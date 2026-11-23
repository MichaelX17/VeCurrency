package com.example.vecurrency.data.remote.bcv

import com.example.vecurrency.data.remote.binance.BinanceP2pHttpClient
import com.example.vecurrency.data.remote.binance.UsdtP2pParser
import com.example.vecurrency.domain.model.Currency
import com.example.vecurrency.domain.model.ExchangeRate
import kotlinx.coroutines.CancellationException
import java.time.Instant

class BcvRemoteSource(
    private val httpClient: BcvHttpClient = BcvHttpClient(),
    private val parser: BcvHtmlParser = BcvHtmlParser,
    private val fallbackHttpClient: BinanceP2pHttpClient? = null
) : BcvSource {

    override suspend fun fetchBcvRates(): Result<List<ExchangeRate>> {
        return try {
            val html = httpClient.fetchBcvHtml()
            val rates = parser.parse(html = html, fetchTime = Instant.now())
            Result.success(rates)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            val fallbackClient = fallbackHttpClient ?: return Result.failure(e)
            // Secondary fallback attempt: DolarApi official endpoint
            return try {
                val usdJson = fallbackClient.fetchDolarApiOfficialUsdRate()
                val eurJson = fallbackClient.fetchDolarApiOfficialEurRate()
                val usdRate = UsdtP2pParser.parseDolarApiOfficialRate(usdJson, Currency.USD, Instant.now())
                val eurRate = UsdtP2pParser.parseDolarApiOfficialRate(eurJson, Currency.EUR, Instant.now())
                Result.success(listOf(usdRate, eurRate))
            } catch (fallbackEx: Exception) {
                if (fallbackEx is CancellationException) throw fallbackEx
                Result.failure(e)
            }
        }
    }
}
