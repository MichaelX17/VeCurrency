package com.example.vecurrency.scheduler

import com.example.vecurrency.data.local.BcvSlotLogDao
import com.example.vecurrency.data.local.BcvSlotLogEntity
import com.example.vecurrency.data.local.ExchangeRateDao
import com.example.vecurrency.data.local.ExchangeRateEntity
import com.example.vecurrency.data.local.LastFetchDao
import com.example.vecurrency.data.local.LastFetchEntity
import com.example.vecurrency.data.remote.bcv.BcvSource
import com.example.vecurrency.data.remote.binance.UsdtSource
import com.example.vecurrency.data.repository.DefaultCurrencyRepository
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
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

class ForegroundRefreshCoordinatorTest {

    private class TestExchangeRateDao : ExchangeRateDao {
        private val state = MutableStateFlow<List<ExchangeRateEntity>>(emptyList())
        override fun getAllRatesFlow(): Flow<List<ExchangeRateEntity>> = state
        override suspend fun getAllRates(): List<ExchangeRateEntity> = state.value
        override suspend fun getRate(code: String): ExchangeRateEntity? = state.value.find { it.currencyCode == code }
        override suspend fun insertRates(rates: List<ExchangeRateEntity>) {
            val map = state.value.associateBy { it.currencyCode }.toMutableMap()
            rates.forEach { map[it.currencyCode] = it }
            state.value = map.values.toList()
        }
    }

    private class TestBcvSlotLogDao : BcvSlotLogDao {
        private val logs = mutableMapOf<String, BcvSlotLogEntity>()
        override suspend fun getSlotLog(date: String, slot: String): BcvSlotLogEntity? = logs["${date}_$slot"]
        override suspend fun getSlotLogById(id: String): BcvSlotLogEntity? = logs[id]
        override suspend fun insertSlotLog(log: BcvSlotLogEntity) {
            logs[log.id] = log
        }
    }

    private class TestLastFetchDao : LastFetchDao {
        private val fetches = mutableMapOf<String, LastFetchEntity>()
        override suspend fun getLastFetch(key: String): LastFetchEntity? = fetches[key]
        override suspend fun insertLastFetch(lastFetch: LastFetchEntity) {
            fetches[lastFetch.sourceKey] = lastFetch
        }
    }

    private class CountingBcvSource : BcvSource {
        var callCount = 0
        var shouldSucceed = true
        override suspend fun fetchBcvRates(): Result<List<ExchangeRate>> {
            callCount++
            return if (shouldSucceed) {
                Result.success(
                    listOf(
                        ExchangeRate(
                            currency = Currency.USD,
                            vesRate = BigDecimal("900.00"),
                            timestamp = Instant.now(),
                            fetchTimestamp = Instant.now(),
                            status = RateStatus.FRESH,
                            source = RateSource.BCV
                        ),
                        ExchangeRate(
                            currency = Currency.EUR,
                            vesRate = BigDecimal("1000.00"),
                            timestamp = Instant.now(),
                            fetchTimestamp = Instant.now(),
                            status = RateStatus.FRESH,
                            source = RateSource.BCV
                        )
                    )
                )
            } else {
                Result.failure(IOException("BCV error"))
            }
        }
    }

    private class CountingUsdtSource : UsdtSource {
        var callCount = 0
        var shouldSucceed = true
        override suspend fun fetchUsdtRate(): Result<ExchangeRate> {
            callCount++
            return if (shouldSucceed) {
                Result.success(
                    ExchangeRate(
                        currency = Currency.USDT,
                        vesRate = BigDecimal("1100.00"),
                        timestamp = Instant.now(),
                        fetchTimestamp = Instant.now(),
                        status = RateStatus.FRESH,
                        source = RateSource.BINANCE_P2P
                    )
                )
            } else {
                Result.failure(IOException("USDT error"))
            }
        }
    }

    private fun createCoordinator(
        instantStr: String,
        bcvSource: CountingBcvSource,
        usdtSource: CountingUsdtSource,
        fetchDao: TestLastFetchDao = TestLastFetchDao(),
        slotDao: TestBcvSlotLogDao = TestBcvSlotLogDao(),
        rateDao: TestExchangeRateDao = TestExchangeRateDao()
    ): Pair<ForegroundRefreshCoordinator, DefaultCurrencyRepository> {
        val instant = Instant.parse(instantStr)
        val clock = Clock.fixed(instant, ZoneId.of("UTC"))
        val caracasZone = ZoneId.of("America/Caracas")

        val repository = DefaultCurrencyRepository(
            bcvSource = bcvSource,
            usdtSource = usdtSource,
            exchangeRateDao = rateDao,
            bcvSlotLogDao = slotDao,
            lastFetchDao = fetchDao
        )

        val coordinator = ForegroundRefreshCoordinator(
            repository = repository,
            clock = clock,
            caracasZoneId = caracasZone
        )

        return Pair(coordinator, repository)
    }

    private fun sampleExchangeRateEntity(currency: Currency, vesRate: String = "900.00"): ExchangeRateEntity {
        return ExchangeRateEntity(
            currencyCode = currency.code,
            vesRate = vesRate,
            timestampEpochMs = Instant.now().toEpochMilli(),
            fetchTimestampEpochMs = Instant.now().toEpochMilli(),
            status = RateStatus.FRESH.name,
            source = RateSource.BCV.name
        )
    }

    @Test
    fun bcvSlot_noDataInDbOutOfSchedule_fetchesBcvBootstrap() = runTest {
        val bcvSource = CountingBcvSource()
        val usdtSource = CountingUsdtSource()
        val fetchDao = TestLastFetchDao()

        // 08:00 AM Caracas time = 12:00:00 UTC (before 09:30 AM slot)
        // No stored rates in DB -> Must fetch BCV bootstrap!
        val (coordinator, _) = createCoordinator("2026-11-22T12:00:00Z", bcvSource, usdtSource, fetchDao)

        coordinator.checkDueRefreshes()

        assertEquals(1, bcvSource.callCount)
    }

    @Test
    fun bcvSlot_oneMinuteBefore0930_doesNotFetchBcvIfCachedExists() = runTest {
        val bcvSource = CountingBcvSource()
        val usdtSource = CountingUsdtSource()
        val fetchDao = TestLastFetchDao()
        val rateDao = TestExchangeRateDao()

        // Seed DB with existing rates
        rateDao.insertRates(listOf(sampleExchangeRateEntity(Currency.USD), sampleExchangeRateEntity(Currency.EUR)))
        fetchDao.insertLastFetch(LastFetchEntity("BCV", lastSuccessEpochMs = Instant.parse("2026-11-22T13:00:00Z").toEpochMilli(), lastAttemptEpochMs = 1000L))

        // 2026-11-22 09:29:00 Caracas time = 2026-11-22 13:29:00 UTC (Caracas is UTC-4)
        val (coordinator, _) = createCoordinator("2026-11-22T13:29:00Z", bcvSource, usdtSource, fetchDao, rateDao = rateDao)

        coordinator.checkDueRefreshes()

        assertEquals(0, bcvSource.callCount)
    }

    @Test
    fun bcvSlot_at0930_fetchesBcvAndLogsSlot() = runTest {
        val bcvSource = CountingBcvSource()
        val usdtSource = CountingUsdtSource()
        val fetchDao = TestLastFetchDao()
        val slotDao = TestBcvSlotLogDao()
        val rateDao = TestExchangeRateDao()

        rateDao.insertRates(listOf(sampleExchangeRateEntity(Currency.USD), sampleExchangeRateEntity(Currency.EUR)))
        fetchDao.insertLastFetch(LastFetchEntity("BCV", lastSuccessEpochMs = 1000L, lastAttemptEpochMs = 1000L))

        // 2026-11-22 09:30:00 Caracas = 2026-11-22 13:30:00 UTC
        val (coordinator, repository) = createCoordinator("2026-11-22T13:30:00Z", bcvSource, usdtSource, fetchDao, slotDao, rateDao)

        coordinator.checkDueRefreshes()

        assertEquals(1, bcvSource.callCount)
        assertTrue(repository.isBcvSlotAttempted("2026-11-22", "09:30"))
    }

    @Test
    fun bcvSlot_duplicateSlotSuppression_doesNotFetchTwice() = runTest {
        val bcvSource = CountingBcvSource()
        val usdtSource = CountingUsdtSource()
        val fetchDao = TestLastFetchDao()
        val slotDao = TestBcvSlotLogDao()
        val rateDao = TestExchangeRateDao()

        rateDao.insertRates(listOf(sampleExchangeRateEntity(Currency.USD), sampleExchangeRateEntity(Currency.EUR)))
        fetchDao.insertLastFetch(LastFetchEntity("BCV", lastSuccessEpochMs = 1000L, lastAttemptEpochMs = 1000L))

        val (coordinator, _) = createCoordinator("2026-11-22T13:30:00Z", bcvSource, usdtSource, fetchDao, slotDao, rateDao)

        coordinator.checkDueRefreshes()
        assertEquals(1, bcvSource.callCount)

        // Second check at 09:35 Caracas
        coordinator.checkDueRefreshes()
        assertEquals(1, bcvSource.callCount) // No duplicate call
    }

    @Test
    fun bcvSlot_multipleMissedSlots_fetchesOnlyLatestSlot() = runTest {
        val bcvSource = CountingBcvSource()
        val usdtSource = CountingUsdtSource()
        val fetchDao = TestLastFetchDao()
        val slotDao = TestBcvSlotLogDao()
        val rateDao = TestExchangeRateDao()

        rateDao.insertRates(listOf(sampleExchangeRateEntity(Currency.USD), sampleExchangeRateEntity(Currency.EUR)))
        fetchDao.insertLastFetch(LastFetchEntity("BCV", lastSuccessEpochMs = 1000L, lastAttemptEpochMs = 1000L))

        // 2026-11-22 22:00:00 Caracas = 2026-11-23 02:00:00 UTC (All slots 09:30, 15:00, 21:30 elapsed)
        val (coordinator, repository) = createCoordinator("2026-11-23T02:00:00Z", bcvSource, usdtSource, fetchDao, slotDao, rateDao)

        coordinator.checkDueRefreshes()

        assertEquals(1, bcvSource.callCount) // Only 1 fetch executed for latest slot
        assertTrue(repository.isBcvSlotAttempted("2026-11-22", "09:30"))
        assertTrue(repository.isBcvSlotAttempted("2026-11-22", "15:00"))
        assertTrue(repository.isBcvSlotAttempted("2026-11-22", "21:30"))
    }

    @Test
    fun usdtSlot_noDataInDb_fetchesUsdtBootstrap() = runTest {
        val bcvSource = CountingBcvSource()
        val usdtSource = CountingUsdtSource()
        val fetchDao = TestLastFetchDao()

        val (coordinator, _) = createCoordinator("2026-11-22T12:00:00Z", bcvSource, usdtSource, fetchDao)

        coordinator.checkDueRefreshes()

        assertEquals(1, usdtSource.callCount)
    }

    @Test
    fun usdtSlot_at1200Slot_fetchesUsdtAndLogsSlot() = runTest {
        val bcvSource = CountingBcvSource()
        val usdtSource = CountingUsdtSource()
        val fetchDao = TestLastFetchDao()
        val slotDao = TestBcvSlotLogDao()
        val rateDao = TestExchangeRateDao()

        rateDao.insertRates(listOf(sampleExchangeRateEntity(Currency.USD), sampleExchangeRateEntity(Currency.EUR), sampleExchangeRateEntity(Currency.USDT)))

        // 2026-11-22 12:00:00 Caracas = 2026-11-22 16:00:00 UTC
        val (coordinator, repository) = createCoordinator("2026-11-22T16:00:00Z", bcvSource, usdtSource, fetchDao, slotDao, rateDao)

        coordinator.checkDueRefreshes()

        assertEquals(1, usdtSource.callCount)
        assertTrue(repository.isUsdtSlotAttempted("2026-11-22", "12:00"))
    }

    @Test
    fun usdtSlot_duplicateSlotSuppression_doesNotFetchTwice() = runTest {
        val bcvSource = CountingBcvSource()
        val usdtSource = CountingUsdtSource()
        val fetchDao = TestLastFetchDao()
        val slotDao = TestBcvSlotLogDao()
        val rateDao = TestExchangeRateDao()

        rateDao.insertRates(listOf(sampleExchangeRateEntity(Currency.USD), sampleExchangeRateEntity(Currency.EUR), sampleExchangeRateEntity(Currency.USDT)))

        val (coordinator, _) = createCoordinator("2026-11-22T16:00:00Z", bcvSource, usdtSource, fetchDao, slotDao, rateDao)

        coordinator.checkDueRefreshes()
        assertEquals(1, usdtSource.callCount)

        // Second check at 12:05 Caracas
        coordinator.checkDueRefreshes()
        assertEquals(1, usdtSource.callCount) // No duplicate call
    }
}
