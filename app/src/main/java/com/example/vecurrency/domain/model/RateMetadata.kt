package com.example.vecurrency.domain.model

import java.math.BigDecimal

data class RateMetadata(
    val askMean: BigDecimal? = null,
    val bidMean: BigDecimal? = null,
    val levelsUsed: Int? = null,
    val valueDateIso: String? = null,
    val errorMessage: String? = null
)
