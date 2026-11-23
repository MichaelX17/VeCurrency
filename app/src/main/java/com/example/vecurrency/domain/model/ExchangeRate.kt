package com.example.vecurrency.domain.model

import java.math.BigDecimal
import java.time.Instant

data class ExchangeRate(
    val currency: Currency,
    val vesRate: BigDecimal,
    val timestamp: Instant,
    val fetchTimestamp: Instant,
    val status: RateStatus,
    val source: RateSource,
    val metadata: RateMetadata? = null
)
