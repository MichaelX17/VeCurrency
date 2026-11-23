package com.example.vecurrency.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface LastFetchDao {

    @Query("SELECT * FROM last_fetches WHERE sourceKey = :key")
    suspend fun getLastFetch(key: String): LastFetchEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLastFetch(lastFetch: LastFetchEntity): Unit
}
