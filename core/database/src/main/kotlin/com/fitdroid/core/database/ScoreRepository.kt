package com.fitdroid.core.database

import com.fitdroid.core.database.dao.ScoreDao
import com.fitdroid.core.database.mapper.toDailyScores
import com.fitdroid.core.model.DailyScores
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface ScoreRepository {
    fun observeInRange(start: LocalDate, endExclusive: LocalDate): Flow<List<DailyScores>>
}

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
class RoomScoreRepository(private val dao: ScoreDao) : ScoreRepository {
    override fun observeInRange(start: LocalDate, endExclusive: LocalDate): Flow<List<DailyScores>> =
        dao.observeInRange(start, endExclusive).map { entities -> entities.map { it.toDailyScores() } }
}
