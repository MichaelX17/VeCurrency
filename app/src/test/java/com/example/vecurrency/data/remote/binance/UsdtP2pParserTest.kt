package com.example.vecurrency.data.remote.binance

import com.example.vecurrency.domain.model.Currency
import com.example.vecurrency.domain.model.RateSource
import com.example.vecurrency.domain.model.RateStatus
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal
import java.time.Instant

class UsdtP2pParserTest {

    @Test
    fun computeRateFromDolarApi_validPrice_calculatesCorrectRate() {
        val response = DolarApiResponse(
            promedio = JsonPrimitive(1086.862734),
            fechaActualizacion = "2026-11-22T21:02:02.954Z"
        )
        val fetchTime = Instant.parse("2026-11-22T12:00:00Z")

        val rate = UsdtP2pParser.computeRateFromDolarApi(response, fetchTime)

        assertEquals(Currency.USDT, rate.currency)
        assertEquals(BigDecimal("1086.8627"), rate.vesRate)
        assertEquals(RateSource.BINANCE_P2P, rate.source)
        assertEquals(RateStatus.FRESH, rate.status)
        assertEquals(Instant.parse("2026-11-22T21:02:02.954Z"), rate.timestamp)
    }

    @Test
    fun computeRateFromYadio_validPrice_calculatesRate() {
        val response = YadioResponse(
            USD = YadioUsdData(rate = JsonPrimitive(1086.794589))
        )
        val fetchTime = Instant.parse("2026-11-22T12:00:00Z")

        val rate = UsdtP2pParser.computeRateFromYadio(response, fetchTime)

        assertEquals(Currency.USDT, rate.currency)
        assertEquals(BigDecimal("1086.7946"), rate.vesRate)
        assertEquals(RateSource.BINANCE_P2P, rate.source)
    }

    @Test(expected = UsdtParseException::class)
    fun computeRateFromDolarApi_nullPrice_throwsUsdtParseException() {
        val response = DolarApiResponse(promedio = null)
        UsdtP2pParser.computeRateFromDolarApi(response)
    }

    @Test(expected = UsdtParseException::class)
    fun parseDolarApiResponse_malformedJson_throwsUsdtParseException() {
        UsdtP2pParser.parseDolarApiResponse("{ invalid json }")
    }
}
