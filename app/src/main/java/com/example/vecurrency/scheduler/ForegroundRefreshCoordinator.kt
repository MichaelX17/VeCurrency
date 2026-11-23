package com.example.vecurrency.scheduler

import com.example.vecurrency.data.repository.DefaultCurrencyRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

class ForegroundRefreshCoordinator(
    private val repository: DefaultCurrencyRepository,
    private val clock: Clock = Clock.systemUTC(),
    private val caracasZoneId: ZoneId = ZoneId.of("America/Caracas")
) {
    companion object {
        val BCV_SLOTS = listOf(
            LocalTime.of(9, 30),
            LocalTime.of(15, 0),
            LocalTime.of(21, 30)
        )
        val USDT_SLOTS = listOf(
            LocalTime.of(0, 0),
            LocalTime.of(4, 0),
            LocalTime.of(8, 0),
            LocalTime.of(12, 0),
            LocalTime.of(16, 0),
            LocalTime.of(20, 0)
        )
        val RETRY_BACKOFF_MS = TimeUnit.MINUTES.toMillis(1)       // 1 minute
        val MANUAL_REFRESH_RATE_LIMIT_MS = TimeUnit.SECONDS.toMillis(10) // 10 seconds
        val TICKER_INTERVAL_MS = TimeUnit.SECONDS.toMillis(30)   // 30 seconds
    }

    private var tickerJob: Job? = null
    private val mutex = Mutex()
    private var lastManualRefreshMs: Long = 0L

    fun onAppForegrounded(scope: CoroutineScope) {
        tickerJob?.cancel()
        tickerJob = scope.launch(Dispatchers.IO) {
            checkDueRefreshes()
            while (isActive) {
                delay(TICKER_INTERVAL_MS)
                checkDueRefreshes()
            }
        }
    }

    fun onAppBackgrounded() {
        tickerJob?.cancel()
        tickerJob = null
    }

    suspend fun checkDueRefreshes() = mutex.withLock {
        val now = Instant.now(clock)
        val nowCaracas = ZonedDateTime.ofInstant(now, caracasZoneId)

        coroutineScope {
            launch { checkBcvDue(nowCaracas) }
            launch { checkUsdtDue(nowCaracas) }
        }
    }

    suspend fun triggerManualRefresh(): Result<Unit> = mutex.withLock {
        val nowMs = Instant.now(clock).toEpochMilli()
        if (nowMs - lastManualRefreshMs < MANUAL_REFRESH_RATE_LIMIT_MS) {
            return Result.failure(IllegalStateException("Refresco manual limitado. Intente de nuevo en unos segundos."))
        }
        lastManualRefreshMs = nowMs

        val (bcvRes, usdtRes) = coroutineScope {
            val bcvDeferred = async { repository.refreshBcv() }
            val usdtDeferred = async { repository.refreshUsdt() }
            bcvDeferred.await() to usdtDeferred.await()
        }

        return if (bcvRes.isSuccess || usdtRes.isSuccess) {
            Result.success(Unit)
        } else {
            Result.failure(bcvRes.exceptionOrNull() ?: usdtRes.exceptionOrNull() ?: Exception("Error al actualizar"))
        }
    }

    private suspend fun checkBcvDue(nowCaracas: ZonedDateTime) {
        val todayDate = nowCaracas.toLocalDate()
        val currentTime = nowCaracas.toLocalTime()

        val hasUsd = repository.hasBcvUsdRate()
        val hasEur = repository.hasBcvEurRate()

        // 1. Exception: If USD or EUR price is NOT stored in DB -> Fetch immediately out of schedule
        if (!hasUsd || !hasEur) {
            val res = repository.refreshBcv()
            if (res.isSuccess) {
                logElapsedSlots(todayDate, currentTime, succeeded = true)
            }
            return
        }

        // 2. Determine elapsed slots today
        val elapsedSlots = BCV_SLOTS.filter { slotTime -> currentTime >= slotTime }
        if (elapsedSlots.isEmpty()) {
            return // No slot has passed today
        }

        // 3. Find un-attempted slots today
        val unattemptedSlots = mutableListOf<LocalTime>()
        for (slot in elapsedSlots) {
            val slotName = formatSlotName(slot)
            val dateStr = todayDate.toString()
            if (!repository.isBcvSlotAttempted(dateStr, slotName)) {
                unattemptedSlots.add(slot)
            }
        }

        if (unattemptedSlots.isEmpty()) {
            return // All elapsed slots today have already been checked
        }

        // 4. Policy: Fetch ONLY the latest unattempted slot
        val latestUnattemptedSlot = unattemptedSlots.last()
        val dateStr = todayDate.toString()

        val res = repository.refreshBcv()
        val success = res.isSuccess

        // Mark all unattempted slots up to latest as attempted to prevent redundant historic calls
        for (slot in unattemptedSlots) {
            repository.logBcvSlotAttempt(
                caracasDate = dateStr,
                slotName = formatSlotName(slot),
                succeeded = if (slot == latestUnattemptedSlot) success else true
            )
        }
    }

    private suspend fun checkUsdtDue(nowCaracas: ZonedDateTime) {
        val todayDate = nowCaracas.toLocalDate()
        val currentTime = nowCaracas.toLocalTime()

        val hasUsdt = repository.hasUsdtRate()

        // 1. Exception: If USDT price is NOT stored in DB -> Fetch immediately out of schedule
        if (!hasUsdt) {
            val res = repository.refreshUsdt()
            if (res.isSuccess) {
                logElapsedUsdtSlots(todayDate, currentTime, succeeded = true)
            }
            return
        }

        // 2. Determine elapsed slots today
        val elapsedSlots = USDT_SLOTS.filter { slotTime -> currentTime >= slotTime }
        if (elapsedSlots.isEmpty()) {
            return // No slot has passed today
        }

        // 3. Find un-attempted slots today
        val unattemptedSlots = mutableListOf<LocalTime>()
        for (slot in elapsedSlots) {
            val slotName = formatSlotName(slot)
            val dateStr = todayDate.toString()
            if (!repository.isUsdtSlotAttempted(dateStr, slotName)) {
                unattemptedSlots.add(slot)
            }
        }

        if (unattemptedSlots.isEmpty()) {
            return // All elapsed slots today have already been checked
        }

        // 4. Policy: Fetch ONLY the latest unattempted slot
        val latestUnattemptedSlot = unattemptedSlots.last()
        val dateStr = todayDate.toString()

        val res = repository.refreshUsdt()
        val success = res.isSuccess

        // Mark all unattempted slots up to latest as attempted to prevent redundant historic calls
        for (slot in unattemptedSlots) {
            repository.logUsdtSlotAttempt(
                caracasDate = dateStr,
                slotName = formatSlotName(slot),
                succeeded = if (slot == latestUnattemptedSlot) success else true
            )
        }
    }

    private suspend fun logElapsedSlots(date: LocalDate, time: LocalTime, succeeded: Boolean) {
        val elapsedSlots = BCV_SLOTS.filter { slot -> time >= slot }
        val dateStr = date.toString()
        for (slot in elapsedSlots) {
            val slotName = formatSlotName(slot)
            if (!repository.isBcvSlotAttempted(dateStr, slotName)) {
                repository.logBcvSlotAttempt(dateStr, slotName, succeeded)
            }
        }
    }

    private suspend fun logElapsedUsdtSlots(date: LocalDate, time: LocalTime, succeeded: Boolean) {
        val elapsedSlots = USDT_SLOTS.filter { slot -> time >= slot }
        val dateStr = date.toString()
        for (slot in elapsedSlots) {
            val slotName = formatSlotName(slot)
            if (!repository.isUsdtSlotAttempted(dateStr, slotName)) {
                repository.logUsdtSlotAttempt(dateStr, slotName, succeeded)
            }
        }
    }

    private fun formatSlotName(slot: LocalTime): String {
        return java.lang.String.format(java.util.Locale.US, "%02d:%02d", slot.hour, slot.minute)
    }
}
