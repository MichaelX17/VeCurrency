package com.example.vecurrency.data.repository

import com.example.vecurrency.domain.model.ExchangeRate
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal

interface CurrencyRepository {
    fun getRatesFlow(): Flow<List<ExchangeRate>>
    suspend fun refreshBcv(): Result<Unit>
    suspend fun refreshUsdt(): Result<Unit>
    suspend fun getFiveDayPrices(currencyCode: String, fallbackRate: BigDecimal?): List<BigDecimal>
}
