package com.gollan.fadesleeptimer.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SleepDao {
    @Insert
    suspend fun insert(session: SleepSession)

    @Query("SELECT * FROM sleep_sessions ORDER BY timestamp_end DESC LIMIT :limit")
    fun getLastSessions(limit: Int): Flow<List<SleepSession>>
    
    @Query("SELECT * FROM sleep_sessions ORDER BY timestamp_end DESC")
    fun getAllSessions(): Flow<List<SleepSession>>
}
