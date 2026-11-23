package com.example.vecurrency.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyRateHistoryDao {

    @Query("SELECT * FROM daily_rate_history WHERE currencyCode = :code ORDER BY dateIso ASC")
    fun getHistoryFlow(code: String): Flow<List<DailyRateHistoryEntity>>

    @Query("SELECT * FROM daily_rate_history WHERE currencyCode = :code ORDER BY dateIso ASC")
    suspend fun getHistory(code: String): List<DailyRateHistoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyRates(rates: List<DailyRateHistoryEntity>)
}
