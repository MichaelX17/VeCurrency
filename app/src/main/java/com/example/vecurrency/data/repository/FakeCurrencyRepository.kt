package com.example.vecurrency.data.repository

import com.example.vecurrency.domain.model.Currency
import com.example.vecurrency.domain.model.ExchangeRate
import com.example.vecurrency.domain.model.RateMetadata
import com.example.vecurrency.domain.model.RateSource
import com.example.vecurrency.domain.model.RateStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.math.BigDecimal
import java.time.Instant

class FakeCurrencyRepository : CurrencyRepository {

    private val ratesFlow = MutableStateFlow<List<ExchangeRate>>(
        listOf(
            ExchangeRate(
                currency = Currency.USD,
                vesRate = BigDecimal("919.8227"),
                timestamp = Instant.now(),
                fetchTimestamp = Instant.now(),
                status = RateStatus.UNAVAILABLE,
                source = RateSource.BCV,
                metadata = RateMetadata(valueDateIso = "2026-11-24")
            ),
            ExchangeRate(
                currency = Currency.EUR,
                vesRate = BigDecimal("1011.1795"),
                timestamp = Instant.now(),
                fetchTimestamp = Instant.now(),
                status = RateStatus.UNAVAILABLE,
                source = RateSource.BCV,
                metadata = RateMetadata(valueDateIso = "2026-11-24")
            ),
            ExchangeRate(
                currency = Currency.USDT,
                vesRate = BigDecimal("1101.0590"),
                timestamp = Instant.now(),
                fetchTimestamp = Instant.now(),
                status = RateStatus.UNAVAILABLE,
                source = RateSource.BINANCE_P2P,
                metadata = RateMetadata(askMean = BigDecimal("1102.50"), bidMean = BigDecimal("1099.60"), levelsUsed = 20)
            )
        )
    )

    override fun getRatesFlow(): Flow<List<ExchangeRate>> = ratesFlow

    override suspend fun refreshBcv(): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun refreshUsdt(): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun getFiveDayPrices(currencyCode: String, fallbackRate: BigDecimal?): List<BigDecimal> {
        val base = fallbackRate ?: BigDecimal("100.00")
        return listOf(base, base, base, base, base)
    }
}
