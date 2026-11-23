package com.example.vecurrency.domain.calculator

import com.example.vecurrency.domain.model.Currency
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.math.BigDecimal

class CurrencyCalculatorTest {

    private val rates = mapOf(
        Currency.USD to BigDecimal("900.00"),
        Currency.EUR to BigDecimal("1000.00"),
        Currency.USDT to BigDecimal("1100.00")
    )

    @Test
    fun convert_usdToEur_correctResult() {
        // 100 USD @ 900 VES = 90,000 VES. 90,000 VES / 1000 EUR = 90.00 EUR
        val conversion = CurrencyCalculator.convert(
            amount = BigDecimal("100.00"),
            from = Currency.USD,
            to = Currency.EUR,
            rates = rates
        )

        assertEquals(BigDecimal("90.00"), conversion.result)
        assertEquals(Currency.USD, conversion.from)
        assertEquals(Currency.EUR, conversion.to)
    }

    @Test
    fun convert_usdToUsdt_correctResult() {
        // 110 USD @ 900 VES = 99,000 VES. 99,000 VES / 1100 = 90.00 USDT
        val conversion = CurrencyCalculator.convert(
            amount = BigDecimal("110.00"),
            from = Currency.USD,
            to = Currency.USDT,
            rates = rates
        )

        assertEquals(BigDecimal("90.00"), conversion.result)
    }

    @Test
    fun convert_vesToUsd_correctResult() {
        // 9000 VES / 900 = 10.00 USD
        val conversion = CurrencyCalculator.convert(
            amount = BigDecimal("9000.00"),
            from = Currency.VES,
            to = Currency.USD,
            rates = rates
        )

        assertEquals(BigDecimal("10.00"), conversion.result)
    }

    @Test
    fun convert_sameCurrency_returnsSameAmount() {
        val conversion = CurrencyCalculator.convert(
            amount = BigDecimal("50.00"),
            from = Currency.USD,
            to = Currency.USD,
            rates = rates
        )

        assertEquals(BigDecimal("50.00"), conversion.result)
    }

    @Test
    fun parseAmount_validInputs_returnsBigDecimal() {
        assertEquals(BigDecimal("100.50"), CurrencyCalculator.parseAmount("100.50"))
        assertEquals(BigDecimal("100.50"), CurrencyCalculator.parseAmount("100,50"))
        assertEquals(BigDecimal("0"), CurrencyCalculator.parseAmount("0"))
    }

    @Test
    fun parseAmount_invalidInputs_returnsNull() {
        assertNull(CurrencyCalculator.parseAmount(""))
        assertNull(CurrencyCalculator.parseAmount("   "))
        assertNull(CurrencyCalculator.parseAmount("abc"))
        assertNull(CurrencyCalculator.parseAmount("-50"))
    }
}
