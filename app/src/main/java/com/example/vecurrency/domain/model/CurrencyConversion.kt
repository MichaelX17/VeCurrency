package com.example.vecurrency.domain.model

import java.math.BigDecimal

data class CurrencyConversion(
    val amount: BigDecimal,
    val from: Currency,
    val to: Currency,
    val result: BigDecimal,
    val rateFromVes: BigDecimal,
    val rateToVes: BigDecimal,
    val effectiveCrossRate: BigDecimal
)
