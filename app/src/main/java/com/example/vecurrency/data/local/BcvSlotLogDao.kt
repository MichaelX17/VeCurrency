package com.example.vecurrency.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface BcvSlotLogDao {

    @Query("SELECT * FROM bcv_slot_logs WHERE caracasDate = :date AND slotName = :slot")
    suspend fun getSlotLog(date: String, slot: String): BcvSlotLogEntity?

    @Query("SELECT * FROM bcv_slot_logs WHERE id = :id")
    suspend fun getSlotLogById(id: String): BcvSlotLogEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSlotLog(log: BcvSlotLogEntity): Unit
}
