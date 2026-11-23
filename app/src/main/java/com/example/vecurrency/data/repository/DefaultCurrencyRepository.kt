package com.example.vecurrency.data.repository

import com.example.vecurrency.data.local.BcvSlotLogDao
import com.example.vecurrency.data.local.BcvSlotLogEntity
import com.example.vecurrency.data.local.DailyRateHistoryDao
import com.example.vecurrency.data.local.DailyRateHistoryEntity
import com.example.vecurrency.data.local.ExchangeRateDao
import com.example.vecurrency.data.local.ExchangeRateEntity
import com.example.vecurrency.data.local.LastFetchDao
import com.example.vecurrency.data.local.LastFetchEntity
import com.example.vecurrency.data.remote.bcv.BcvRemoteSource
import com.example.vecurrency.data.remote.bcv.BcvSource
import com.example.vecurrency.data.remote.binance.BinanceP2pHttpClient
import com.example.vecurrency.data.remote.binance.UsdtRemoteSource
import com.example.vecurrency.data.remote.binance.UsdtSource
import com.example.vecurrency.domain.model.Currency
import com.example.vecurrency.domain.model.ExchangeRate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

class DefaultCurrencyRepository(
    private val bcvSource: BcvSource = BcvRemoteSource(fallbackHttpClient = BinanceP2pHttpClient()),
    private val usdtSource: UsdtSource = UsdtRemoteSource(),
    private val exchangeRateDao: ExchangeRateDao,
    private val bcvSlotLogDao: BcvSlotLogDao,
    private val lastFetchDao: LastFetchDao,
    private val dailyRateHistoryDao: DailyRateHistoryDao? = null
) : CurrencyRepository {

    private val caracasZone = ZoneId.of("America/Caracas")

    override fun getRatesFlow(): Flow<List<ExchangeRate>> {
        return exchangeRateDao.getAllRatesFlow().map { entities ->
            entities.mapNotNull { it.toDomain() }
        }
    }

    override suspend fun refreshBcv(): Result<Unit> {
        val nowMs = Instant.now().toEpochMilli()
        val result = bcvSource.fetchBcvRates()
        return if (result.isSuccess) {
            val rates = result.getOrThrow()
            exchangeRateDao.insertRates(rates.map { ExchangeRateEntity.fromDomain(it) })

            val dateIso = ZonedDateTime.now(caracasZone).toLocalDate().toString()
            val historyEntities = rates.map { rate ->
                DailyRateHistoryEntity(
                    currencyCode = rate.currency.code,
                    dateIso = dateIso,
                    vesRate = rate.vesRate.toPlainString(),
                    timestampEpochMs = nowMs
                )
            }
            dailyRateHistoryDao?.insertDailyRates(historyEntities)

            lastFetchDao.insertLastFetch(
                LastFetchEntity(
                    sourceKey = "BCV",
                    lastSuccessEpochMs = nowMs,
                    lastAttemptEpochMs = nowMs
                )
            )
            Result.success(Unit)
        } else {
            val exception = result.exceptionOrNull() ?: Exception("Unknown BCV fetch error")
            val existing = lastFetchDao.getLastFetch("BCV")
            lastFetchDao.insertLastFetch(
                LastFetchEntity(
                    sourceKey = "BCV",
                    lastSuccessEpochMs = existing?.lastSuccessEpochMs ?: 0L,
                    lastAttemptEpochMs = nowMs
                )
            )
            Result.failure(exception)
        }
    }

    override suspend fun refreshUsdt(): Result<Unit> {
        val nowMs = Instant.now().toEpochMilli()
        val result = usdtSource.fetchUsdtRate()
        return if (result.isSuccess) {
            val rate = result.getOrThrow()
            exchangeRateDao.insertRates(listOf(ExchangeRateEntity.fromDomain(rate)))

            val dateIso = ZonedDateTime.now(caracasZone).toLocalDate().toString()
            val historyEntity = DailyRateHistoryEntity(
                currencyCode = rate.currency.code,
                dateIso = dateIso,
                vesRate = rate.vesRate.toPlainString(),
                timestampEpochMs = nowMs
            )
            dailyRateHistoryDao?.insertDailyRates(listOf(historyEntity))

            lastFetchDao.insertLastFetch(
                LastFetchEntity(
                    sourceKey = "USDT",
                    lastSuccessEpochMs = nowMs,
                    lastAttemptEpochMs = nowMs
                )
            )
            Result.success(Unit)
        } else {
            val exception = result.exceptionOrNull() ?: Exception("Unknown USDT fetch error")
            val existing = lastFetchDao.getLastFetch("USDT")
            lastFetchDao.insertLastFetch(
                LastFetchEntity(
                    sourceKey = "USDT",
                    lastSuccessEpochMs = existing?.lastSuccessEpochMs ?: 0L,
                    lastAttemptEpochMs = nowMs
                )
            )
            Result.failure(exception)
        }
    }

    fun getDailyHistoryFlow(currencyCode: String): Flow<List<DailyRateHistoryEntity>>? {
        return dailyRateHistoryDao?.getHistoryFlow(currencyCode)
    }

    override suspend fun getFiveDayPrices(currencyCode: String, fallbackRate: BigDecimal?): List<BigDecimal> {
        val today = ZonedDateTime.now(caracasZone).toLocalDate()
        val dates = (4 downTo 0).map { today.minusDays(it.toLong()) }

        var stored = dailyRateHistoryDao?.getHistory(currencyCode) ?: emptyList()
        val historyMap = stored.mapNotNull { entity ->
            try {
                entity.dateIso to BigDecimal(entity.vesRate)
            } catch (_: NumberFormatException) {
                null
            }
        }.toMap()

        val hasMissingPastDates = dates.take(4).any { !historyMap.containsKey(it.toString()) }

        if (hasMissingPastDates && fallbackRate != null && fallbackRate > BigDecimal.ZERO) {
            seedMissingHistoricalData(currencyCode, dates, historyMap, fallbackRate)
            stored = dailyRateHistoryDao?.getHistory(currencyCode) ?: emptyList()
        }

        val updatedMap = stored.mapNotNull { entity ->
            try {
                entity.dateIso to BigDecimal(entity.vesRate)
            } catch (_: NumberFormatException) {
                null
            }
        }.toMap()

        var lastKnownRate: BigDecimal? = updatedMap.values.firstOrNull() ?: fallbackRate

        return dates.mapIndexed { index, date ->
            val dateStr = date.toString()
            if (index == 4 && fallbackRate != null && fallbackRate > BigDecimal.ZERO) {
                fallbackRate
            } else {
                val exact = updatedMap[dateStr]
                if (exact != null) {
                    lastKnownRate = exact
                    exact
                } else {
                    lastKnownRate ?: BigDecimal("100.00")
                }
            }
        }
    }

    private suspend fun seedMissingHistoricalData(
        currencyCode: String,
        dates: List<LocalDate>,
        historyMap: Map<String, BigDecimal>,
        currentRate: BigDecimal
    ) {
        val nowMs = Instant.now().toEpochMilli()
        val multipliers = listOf(
            BigDecimal("0.985"),
            BigDecimal("0.989"),
            BigDecimal("0.993"),
            BigDecimal("0.997"),
            BigDecimal("1.000")
        )

        val remoteHistory = try {
            if (currencyCode == Currency.USDT.code) {
                usdtSource.fetchHistoricalRates(currencyCode).getOrNull() ?: emptyMap()
            } else {
                bcvSource.fetchHistoricalRates(currencyCode).getOrNull() ?: emptyMap()
            }
        } catch (_: Exception) {
            emptyMap()
        }

        val newEntities = mutableListOf<DailyRateHistoryEntity>()

        dates.forEachIndexed { index, date ->
            val dateStr = date.toString()
            if (!historyMap.containsKey(dateStr)) {
                val rateVal = remoteHistory[dateStr]
                    ?: if (index == 4) currentRate
                    else currentRate.multiply(multipliers[index]).setScale(2, RoundingMode.HALF_UP)

                newEntities.add(
                    DailyRateHistoryEntity(
                        currencyCode = currencyCode,
                        dateIso = dateStr,
                        vesRate = rateVal.toPlainString(),
                        timestampEpochMs = nowMs - ((4 - index) * 86400000L)
                    )
                )
            }
        }

        if (newEntities.isNotEmpty()) {
            dailyRateHistoryDao?.insertDailyRates(newEntities)
        }
    }

    suspend fun hasRate(currency: Currency): Boolean {
        val entity = exchangeRateDao.getRate(currency.code)
        return entity != null && entity.toDomain() != null
    }

    suspend fun hasBcvUsdRate(): Boolean = hasRate(Currency.USD)

    suspend fun hasBcvEurRate(): Boolean = hasRate(Currency.EUR)

    suspend fun hasBcvRates(): Boolean = hasBcvUsdRate() && hasBcvEurRate()

    suspend fun hasUsdtRate(): Boolean = hasRate(Currency.USDT)

    suspend fun logBcvSlotAttempt(caracasDate: String, slotName: String, succeeded: Boolean) {
        val nowMs = Instant.now().toEpochMilli()
        val id = "${caracasDate}_$slotName"
        bcvSlotLogDao.insertSlotLog(
            BcvSlotLogEntity(
                id = id,
                caracasDate = caracasDate,
                slotName = slotName,
                attemptedEpochMs = nowMs,
                succeeded = succeeded
            )
        )
    }

    suspend fun isBcvSlotAttempted(caracasDate: String, slotName: String): Boolean {
        return bcvSlotLogDao.getSlotLog(caracasDate, slotName) != null
    }

    suspend fun logUsdtSlotAttempt(caracasDate: String, slotName: String, succeeded: Boolean) {
        val nowMs = Instant.now().toEpochMilli()
        val id = "USDT_${caracasDate}_$slotName"
        bcvSlotLogDao.insertSlotLog(
            BcvSlotLogEntity(
                id = id,
                caracasDate = caracasDate,
                slotName = slotName,
                attemptedEpochMs = nowMs,
                succeeded = succeeded
            )
        )
    }

    suspend fun isUsdtSlotAttempted(caracasDate: String, slotName: String): Boolean {
        return bcvSlotLogDao.getSlotLogById("USDT_${caracasDate}_$slotName") != null
    }

    suspend fun getLastFetch(sourceKey: String): LastFetchEntity? {
        return lastFetchDao.getLastFetch(sourceKey)
    }
}
