package com.example.vecurrency.data.remote.binance

import com.example.vecurrency.domain.model.ExchangeRate
import java.math.BigDecimal

interface UsdtSource {
    suspend fun fetchUsdtRate(): Result<ExchangeRate>
    suspend fun fetchHistoricalRates(currencyCode: String): Result<Map<String, BigDecimal>> = Result.success(emptyMap())
}
