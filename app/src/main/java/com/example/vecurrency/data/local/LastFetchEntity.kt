package com.example.vecurrency.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "last_fetches")
data class LastFetchEntity(
    @PrimaryKey val sourceKey: String, // e.g. "BCV", "USDT"
    val lastSuccessEpochMs: Long,
    val lastAttemptEpochMs: Long
)
