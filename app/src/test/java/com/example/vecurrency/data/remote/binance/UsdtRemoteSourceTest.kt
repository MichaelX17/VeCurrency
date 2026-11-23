package com.example.vecurrency.data.remote.binance

import com.example.vecurrency.domain.model.Currency
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException
import java.math.BigDecimal

class UsdtRemoteSourceTest {

    private val sampleDolarApiJson = """
        {
          "moneda": "USD",
          "fuente": "paralelo",
          "nombre": "Paralelo",
          "promedio": 1086.862734,
          "fechaActualizacion": "2026-11-22T21:02:02.954Z"
        }
    """.trimIndent()

    private val sampleYadioJson = """
        {
          "USD": {
            "rate": 1086.794589
          }
        }
    """.trimIndent()

    @Test
    fun fetchUsdtRate_dolarApiSuccess_returnsDolarApiRate() = runTest {
        val fakeClient = object : BinanceP2pHttpClient() {
            override suspend fun fetchDolarApiUsdtRate(): String {
                return sampleDolarApiJson
            }
        }

        val source = UsdtRemoteSource(httpClient = fakeClient)
        val result = source.fetchUsdtRate()

        assertTrue(result.isSuccess)
        val rate = result.getOrThrow()
        assertEquals(Currency.USDT, rate.currency)
        assertEquals(BigDecimal("1086.8627"), rate.vesRate)
    }

    @Test
    fun fetchUsdtRate_dolarApiFails_usesYadioFallback() = runTest {
        val fakeClient = object : BinanceP2pHttpClient() {
            override suspend fun fetchDolarApiUsdtRate(): String {
                throw IOException("DolarApi down")
            }

            override suspend fun fetchYadioUsdtRate(): String {
                return sampleYadioJson
            }
        }

        val source = UsdtRemoteSource(httpClient = fakeClient)
        val result = source.fetchUsdtRate()

        assertTrue(result.isSuccess)
        val rate = result.getOrThrow()
        assertEquals(Currency.USDT, rate.currency)
        assertEquals(BigDecimal("1086.7946"), rate.vesRate)
    }

    @Test
    fun fetchUsdtRate_bothSourcesFail_returnsFailure() = runTest {
        val fakeClient = object : BinanceP2pHttpClient() {
            override suspend fun fetchDolarApiUsdtRate(): String {
                throw IOException("DolarApi down")
            }

            override suspend fun fetchYadioUsdtRate(): String {
                throw IOException("Yadio API down")
            }
        }

        val source = UsdtRemoteSource(httpClient = fakeClient)
        val result = source.fetchUsdtRate()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IOException)
    }
}
