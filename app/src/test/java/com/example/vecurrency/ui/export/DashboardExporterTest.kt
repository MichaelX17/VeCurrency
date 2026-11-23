package com.example.vecurrency.ui.export

import com.example.vecurrency.domain.model.Currency
import com.example.vecurrency.domain.model.ExchangeRate
import com.example.vecurrency.domain.model.RateSource
import com.example.vecurrency.domain.model.RateStatus
import org.junit.Test
import java.math.BigDecimal
import java.time.Instant

class DashboardExporterTest {

    @Test
    fun createDashboardBitmap_handlesExecutionSafely() {
        val sampleRates = listOf(
            ExchangeRate(
                currency = Currency.USD,
                vesRate = BigDecimal("919.8227"),
                timestamp = Instant.now(),
                fetchTimestamp = Instant.now(),
                status = RateStatus.FRESH,
                source = RateSource.BCV
            ),
            ExchangeRate(
                currency = Currency.EUR,
                vesRate = BigDecimal("1011.1795"),
                timestamp = Instant.now(),
                fetchTimestamp = Instant.now(),
                status = RateStatus.FRESH,
                source = RateSource.BCV
            ),
            ExchangeRate(
                currency = Currency.USDT,
                vesRate = BigDecimal("1101.0590"),
                timestamp = Instant.now(),
                fetchTimestamp = Instant.now(),
                status = RateStatus.FRESH,
                source = RateSource.BINANCE_P2P
            )
        )

        // Verifies calling createDashboardBitmap doesn't throw unhandled exceptions
        val bitmap = DashboardExporter.createDashboardBitmap(sampleRates)
        // On unmocked JVM stub returns null safely without crashing
    }
}
