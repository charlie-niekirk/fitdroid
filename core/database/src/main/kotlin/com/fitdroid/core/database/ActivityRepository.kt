package com.fitdroid.core.database

import com.fitdroid.core.database.dao.DailyMetricsDao
import com.fitdroid.core.database.dao.ExerciseSessionDao
import com.fitdroid.core.database.mapper.toModel
import com.fitdroid.core.model.DailyMetrics
import com.fitdroid.core.model.ExerciseSession
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import java.time.Instant
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface ActivityRepository {
    fun observeMetrics(start: LocalDate, endExclusive: LocalDate): Flow<List<DailyMetrics>>

    fun observeExercise(start: Instant, end: Instant): Flow<List<ExerciseSession>>
}

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
class RoomActivityRepository(
    private val metricsDao: DailyMetricsDao,
    private val exerciseDao: ExerciseSessionDao,
) : ActivityRepository {
    override fun observeMetrics(start: LocalDate, endExclusive: LocalDate): Flow<List<DailyMetrics>> =
        metricsDao.observeInRange(start, endExclusive).map { rows -> rows.map { it.toModel() } }

    override fun observeExercise(start: Instant, end: Instant): Flow<List<ExerciseSession>> =
        exerciseDao.observeInRange(start, end).map { rows -> rows.map { it.toModel() } }
}
