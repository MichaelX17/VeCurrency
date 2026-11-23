package com.example.vecurrency.data.remote.bcv

import com.example.vecurrency.domain.model.Currency
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException
import java.math.BigDecimal

class BcvRemoteSourceTest {

    @Test
    fun fetchBcvRates_successfulHtml_returnsSuccessWithRates() = runTest {
        val sampleHtml = """
            <!DOCTYPE html>
            <html lang="es">
            <body>
              <div id="dolar"><strong class="strong-tb">919,82270000</strong></div>
              <div id="euro"><strong class="strong-tb">1.011,17949056</strong></div>
              <span property="dc:date" content="2026-11-24T00:00:00-04:00">24 Nov 2026</span>
            </body>
            </html>
        """.trimIndent()

        val fakeClient = object : BcvHttpClient() {
            override suspend fun fetchBcvHtml(): String = sampleHtml
        }

        val source = BcvRemoteSource(httpClient = fakeClient)
        val result = source.fetchBcvRates()

        assertTrue(result.isSuccess)
        val rates = result.getOrThrow()
        assertEquals(2, rates.size)

        val usd = rates.first { it.currency == Currency.USD }
        assertEquals(BigDecimal("919.82270000"), usd.vesRate)

        val eur = rates.first { it.currency == Currency.EUR }
        assertEquals(BigDecimal("1011.17949056"), eur.vesRate)
    }

    @Test
    fun fetchBcvRates_networkException_returnsFailure() = runTest {
        val fakeClient = object : BcvHttpClient() {
            override suspend fun fetchBcvHtml(): String {
                throw IOException("Network timeout connecting to BCV")
            }
        }

        val source = BcvRemoteSource(httpClient = fakeClient)
        val result = source.fetchBcvRates()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IOException)
    }

    @Test
    fun fetchBcvRates_invalidHtml_returnsFailure() = runTest {
        val fakeClient = object : BcvHttpClient() {
            override suspend fun fetchBcvHtml(): String = "<html><body>No rate data</body></html>"
        }

        val source = BcvRemoteSource(httpClient = fakeClient)
        val result = source.fetchBcvRates()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is BcvParseException)
    }

    @Test
    fun fetchBcvRates_htmlFails_usesDolarApiFallback() = runTest {
        val fakeBcvClient = object : BcvHttpClient() {
            override suspend fun fetchBcvHtml(): String {
                throw IOException("BCV website unavailable")
            }
        }

        val fakeFallbackClient = object : com.example.vecurrency.data.remote.binance.BinanceP2pHttpClient() {
            override suspend fun fetchDolarApiOfficialUsdRate(): String {
                return """{"moneda":"USD","fuente":"oficial","nombre":"Oficial","promedio":919.8227,"fechaActualizacion":"2026-11-24T00:00:00-04:00"}"""
            }
            override suspend fun fetchDolarApiOfficialEurRate(): String {
                return """{"moneda":"EUR","fuente":"oficial","nombre":"Oficial","promedio":1011.1795,"fechaActualizacion":"2026-11-24T00:00:00-04:00"}"""
            }
        }

        val source = BcvRemoteSource(httpClient = fakeBcvClient, fallbackHttpClient = fakeFallbackClient)
        val result = source.fetchBcvRates()

        assertTrue(result.isSuccess)
        val rates = result.getOrThrow()
        assertEquals(2, rates.size)

        val usd = rates.first { it.currency == Currency.USD }
        assertEquals(BigDecimal("919.8227"), usd.vesRate)
    }
}
