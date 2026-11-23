package com.example.vecurrency.data.remote.bcv

import com.example.vecurrency.domain.model.ExchangeRate
import java.math.BigDecimal

interface BcvSource {
    suspend fun fetchBcvRates(): Result<List<ExchangeRate>>
    suspend fun fetchHistoricalRates(currencyCode: String): Result<Map<String, BigDecimal>> = Result.success(emptyMap())
}
