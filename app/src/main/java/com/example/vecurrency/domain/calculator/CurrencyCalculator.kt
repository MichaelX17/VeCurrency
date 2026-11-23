package com.example.vecurrency.domain.calculator

import com.example.vecurrency.domain.model.Currency
import com.example.vecurrency.domain.model.CurrencyConversion
import java.math.BigDecimal
import java.math.RoundingMode

object CurrencyCalculator {

    private const val INTERNAL_SCALE = 10
    private const val DISPLAY_SCALE = 2

    fun convert(
        amount: BigDecimal,
        from: Currency,
        to: Currency,
        rates: Map<Currency, BigDecimal>
    ): CurrencyConversion {
        require(amount >= BigDecimal.ZERO) { "Amount cannot be negative" }

        if (from == to) {
            val one = BigDecimal.ONE
            val rate = if (from == Currency.VES) BigDecimal.ONE else rates[from] ?: BigDecimal.ONE
            return CurrencyConversion(
                amount = amount,
                from = from,
                to = to,
                result = amount.setScale(DISPLAY_SCALE, RoundingMode.HALF_UP),
                rateFromVes = rate,
                rateToVes = rate,
                effectiveCrossRate = one
            )
        }

        val rateFromVes = getVesRate(from, rates)
        val rateToVes = getVesRate(to, rates)

        require(rateFromVes > BigDecimal.ZERO) { "Rate for ${from.code} must be greater than zero" }
        require(rateToVes > BigDecimal.ZERO) { "Rate for ${to.code} must be greater than zero" }

        // VES Value = amount * rateFromVes
        val vesValue = amount.multiply(rateFromVes)

        // Result = VES Value / rateToVes
        val result = vesValue.divide(rateToVes, INTERNAL_SCALE, RoundingMode.HALF_UP)
            .setScale(DISPLAY_SCALE, RoundingMode.HALF_UP)

        // Effective Cross Rate = rateFromVes / rateToVes
        val crossRate = rateFromVes.divide(rateToVes, INTERNAL_SCALE, RoundingMode.HALF_UP)

        return CurrencyConversion(
            amount = amount,
            from = from,
            to = to,
            result = result,
            rateFromVes = rateFromVes,
            rateToVes = rateToVes,
            effectiveCrossRate = crossRate
        )
    }

    private fun getVesRate(currency: Currency, rates: Map<Currency, BigDecimal>): BigDecimal {
        return if (currency == Currency.VES) {
            BigDecimal.ONE
        } else {
            rates[currency] ?: throw IllegalArgumentException("Missing rate for ${currency.code}")
        }
    }

    fun parseAmount(input: String): BigDecimal? {
        if (input.isBlank()) return null
        val normalized = input.trim().replace(',', '.')
        return try {
            val bd = BigDecimal(normalized)
            if (bd >= BigDecimal.ZERO) bd else null
        } catch (_: NumberFormatException) {
            null
        }
    }
}
