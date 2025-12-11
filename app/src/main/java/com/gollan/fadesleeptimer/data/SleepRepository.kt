package com.gollan.fadesleeptimer.data

import com.gollan.fadesleeptimer.data.local.SleepDao
import com.gollan.fadesleeptimer.data.local.SleepSession
import kotlinx.coroutines.flow.Flow

class SleepRepository(private val sleepDao: SleepDao) {
    val lastSessions: Flow<List<SleepSession>> = sleepDao.getLastSessions(7)

    suspend fun insert(session: SleepSession) {
        sleepDao.insert(session)
    }
}
