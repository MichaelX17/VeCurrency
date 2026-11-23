package com.example.vecurrency.data.local

import androidx.room.Entity

@Entity(
    tableName = "daily_rate_history",
    primaryKeys = ["currencyCode", "dateIso"]
)
data class DailyRateHistoryEntity(
    val currencyCode: String,
    val dateIso: String,
    val vesRate: String,
    val timestampEpochMs: Long
)
