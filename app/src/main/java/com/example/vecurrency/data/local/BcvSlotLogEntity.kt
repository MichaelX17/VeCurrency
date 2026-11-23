package com.example.vecurrency.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bcv_slot_logs")
data class BcvSlotLogEntity(
    @PrimaryKey val id: String, // e.g. "2026-11-22_09:30"
    val caracasDate: String,     // e.g. "2026-11-22"
    val slotName: String,        // e.g. "09:30", "15:00", "21:30"
    val attemptedEpochMs: Long,
    val succeeded: Boolean
)
