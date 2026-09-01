package com.fitdroid.core.database

import com.fitdroid.core.database.dao.SleepSessionDao
import com.fitdroid.core.database.mapper.toModel
import com.fitdroid.core.model.SleepSession
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import java.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface SleepRepository {
    fun observeInRange(start: Instant, end: Instant): Flow<List<SleepSession>>
}

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
class RoomSleepRepository(private val dao: SleepSessionDao) : SleepRepository {
    override fun observeInRange(start: Instant, end: Instant): Flow<List<SleepSession>> =
        dao.observeInRange(start, end).map { sessions -> sessions.map { it.toModel() } }
}
