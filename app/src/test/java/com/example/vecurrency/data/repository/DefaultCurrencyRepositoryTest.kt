package com.example.vecurrency.data.repository

import com.example.vecurrency.data.local.BcvSlotLogDao
import com.example.vecurrency.data.local.BcvSlotLogEntity
import com.example.vecurrency.data.local.ExchangeRateDao
import com.example.vecurrency.data.local.ExchangeRateEntity
import com.example.vecurrency.data.local.LastFetchDao
import com.example.vecurrency.data.local.LastFetchEntity
import com.example.vecurrency.data.remote.bcv.BcvSource
import com.example.vecurrency.data.remote.binance.UsdtSource
import com.example.vecurrency.domain.model.Currency
import com.example.vecurrency.domain.model.ExchangeRate
import com.example.vecurrency.domain.model.RateSource
import com.example.vecurrency.domain.model.RateStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException
import java.math.BigDecimal
import java.time.Instant

class DefaultCurrencyRepositoryTest {

    private class FakeExchangeRateDao : ExchangeRateDao {
        private val state = MutableStateFlow<List<ExchangeRateEntity>>(emptyList())
        override fun getAllRatesFlow(): Flow<List<ExchangeRateEntity>> = state
        override suspend fun getAllRates(): List<ExchangeRateEntity> = state.value
        override suspend fun getRate(code: String): ExchangeRateEntity? = state.value.find { it.currencyCode == code }
        override suspend fun insertRates(rates: List<ExchangeRateEntity>) {
            val currentMap = state.value.associateBy { it.currencyCode }.toMutableMap()
            rates.forEach { currentMap[it.currencyCode] = it }
            state.value = currentMap.values.toList()
        }
    }

    private class FakeBcvSlotLogDao : BcvSlotLogDao {
        private val logs = mutableMapOf<String, BcvSlotLogEntity>()
        override suspend fun getSlotLog(date: String, slot: String): BcvSlotLogEntity? = logs["${date}_$slot"]
        override suspend fun getSlotLogById(id: String): BcvSlotLogEntity? = logs[id]
        override suspend fun insertSlotLog(log: BcvSlotLogEntity) {
            logs[log.id] = log
        }
    }

    private class FakeLastFetchDao : LastFetchDao {
        private val fetches = mutableMapOf<String, LastFetchEntity>()
        override suspend fun getLastFetch(key: String): LastFetchEntity? = fetches[key]
        override suspend fun insertLastFetch(lastFetch: LastFetchEntity) {
            fetches[lastFetch.sourceKey] = lastFetch
        }
    }

    private class FakeDailyRateHistoryDao : com.example.vecurrency.data.local.DailyRateHistoryDao {
        private val history = mutableListOf<com.example.vecurrency.data.local.DailyRateHistoryEntity>()
        override fun getHistoryFlow(code: String): Flow<List<com.example.vecurrency.data.local.DailyRateHistoryEntity>> = MutableStateFlow(history)
        override suspend fun getHistory(code: String): List<com.example.vecurrency.data.local.DailyRateHistoryEntity> = history.filter { it.currencyCode == code }
        override suspend fun insertDailyRates(rates: List<com.example.vecurrency.data.local.DailyRateHistoryEntity>) {
            history.addAll(rates)
        }
    }

    private class FakeBcvSource(var result: Result<List<ExchangeRate>>) : BcvSource {
        override suspend fun fetchBcvRates(): Result<List<ExchangeRate>> = result
    }

    private class FakeUsdtSource(var result: Result<ExchangeRate>) : UsdtSource {
        override suspend fun fetchUsdtRate(): Result<ExchangeRate> = result
    }

    @Test
    fun refreshBcv_success_persistsRatesAndUpdatesTimestamp() = runTest {
        val rateUsd = ExchangeRate(
            currency = Currency.USD,
            vesRate = BigDecimal("900.00"),
            timestamp = Instant.now(),
            fetchTimestamp = Instant.now(),
            status = RateStatus.FRESH,
            source = RateSource.BCV
        )
        val bcvSource = FakeBcvSource(Result.success(listOf(rateUsd)))
        val usdtSource = FakeUsdtSource(Result.failure(Exception()))
        val rateDao = FakeExchangeRateDao()
        val slotDao = FakeBcvSlotLogDao()
        val fetchDao = FakeLastFetchDao()

        val repository = DefaultCurrencyRepository(
            bcvSource = bcvSource,
            usdtSource = usdtSource,
            exchangeRateDao = rateDao,
            bcvSlotLogDao = slotDao,
            lastFetchDao = fetchDao
        )

        val refreshResult = repository.refreshBcv()
        assertTrue(refreshResult.isSuccess)

        val cached = rateDao.getRate("USD")
        assertEquals("900.00", cached?.vesRate)

        val lastFetch = fetchDao.getLastFetch("BCV")
        assertTrue((lastFetch?.lastSuccessEpochMs ?: 0L) > 0L)
    }

    @Test
    fun refreshBcv_failure_preservesPreviousCachedRates() = runTest {
        val rateUsd = ExchangeRate(
            currency = Currency.USD,
            vesRate = BigDecimal("900.00"),
            timestamp = Instant.now(),
            fetchTimestamp = Instant.now(),
            status = RateStatus.FRESH,
            source = RateSource.BCV
        )
        val rateDao = FakeExchangeRateDao()
        rateDao.insertRates(listOf(ExchangeRateEntity.fromDomain(rateUsd)))

        val bcvSource = FakeBcvSource(Result.failure(IOException("Network error")))
        val usdtSource = FakeUsdtSource(Result.failure(Exception()))
        val slotDao = FakeBcvSlotLogDao()
        val fetchDao = FakeLastFetchDao()

        // Set initial last fetch with success = 1000L
        fetchDao.insertLastFetch(LastFetchEntity("BCV", lastSuccessEpochMs = 1000L, lastAttemptEpochMs = 1000L))

        val repository = DefaultCurrencyRepository(
            bcvSource = bcvSource,
            usdtSource = usdtSource,
            exchangeRateDao = rateDao,
            bcvSlotLogDao = slotDao,
            lastFetchDao = fetchDao
        )

        val refreshResult = repository.refreshBcv()
        assertTrue(refreshResult.isFailure)

        // Cached rate must be untouched
        val cached = rateDao.getRate("USD")
        assertEquals("900.00", cached?.vesRate)

        // Success timestamp must be untouched (still 1000L), but attempt timestamp updated
        val lastFetch = fetchDao.getLastFetch("BCV")
        assertEquals(1000L, lastFetch?.lastSuccessEpochMs)
        assertTrue((lastFetch?.lastAttemptEpochMs ?: 0L) > 1000L)
    }

    @Test
    fun getFiveDayPrices_returnsFivePointsWithTodayAsLatest() = runTest {
        val bcvSource = FakeBcvSource(Result.failure(Exception()))
        val usdtSource = FakeUsdtSource(Result.failure(Exception()))
        val rateDao = FakeExchangeRateDao()
        val slotDao = FakeBcvSlotLogDao()
        val fetchDao = FakeLastFetchDao()
        val historyDao = FakeDailyRateHistoryDao()

        val repository = DefaultCurrencyRepository(
            bcvSource = bcvSource,
            usdtSource = usdtSource,
            exchangeRateDao = rateDao,
            bcvSlotLogDao = slotDao,
            lastFetchDao = fetchDao,
            dailyRateHistoryDao = historyDao
        )

        val result = repository.getFiveDayPrices("USD", BigDecimal("950.00"))
        assertEquals(5, result.size)
        assertEquals(BigDecimal("950.00"), result[4]) // Latest entry (today) shows current price
    }
}
