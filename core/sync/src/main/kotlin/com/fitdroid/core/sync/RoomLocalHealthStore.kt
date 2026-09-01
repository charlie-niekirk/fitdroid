package com.fitdroid.core.sync

import com.fitdroid.core.database.dao.DailyMetricsDao
import com.fitdroid.core.database.dao.ExerciseSessionDao
import com.fitdroid.core.database.dao.HeartRateSampleDao
import com.fitdroid.core.database.dao.ScoreDao
import com.fitdroid.core.database.dao.SleepSessionDao
import com.fitdroid.core.database.dao.SyncStateDao
import com.fitdroid.core.database.entity.SyncStateEntity
import com.fitdroid.core.database.mapper.stageEntities
import com.fitdroid.core.database.mapper.toEntity
import com.fitdroid.core.database.mapper.toModel
import com.fitdroid.core.model.DailyMetrics
import com.fitdroid.core.model.DailyScores
import com.fitdroid.core.model.ExerciseSession
import com.fitdroid.core.model.HeartRateSample
import com.fitdroid.core.model.SleepSession
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import java.time.Instant
import java.time.LocalDate

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
class RoomLocalHealthStore(
    private val sleepSessionDao: SleepSessionDao,
    private val exerciseSessionDao: ExerciseSessionDao,
    private val heartRateSampleDao: HeartRateSampleDao,
    private val dailyMetricsDao: DailyMetricsDao,
    private val scoreDao: ScoreDao,
    private val syncStateDao: SyncStateDao,
) : LocalHealthStore {
    override suspend fun upsertSleep(session: SleepSession) {
        sleepSessionDao.upsert(session.toEntity(), session.stageEntities())
    }

    override suspend fun replaceSleep(start: Instant, end: Instant, sessions: List<SleepSession>) {
        sleepSessionDao.deleteInRange(start, end)
        sessions.forEach { upsertSleep(it) }
    }

    override suspend fun sleepInRange(start: Instant, end: Instant): List<SleepSession> =
        sleepSessionDao.getInRange(start, end).map { it.toModel() }

    override suspend fun upsertExercise(session: ExerciseSession) {
        exerciseSessionDao.upsert(session.toEntity())
    }

    override suspend fun replaceExercise(
        start: Instant,
        end: Instant,
        sessions: List<ExerciseSession>,
    ) {
        exerciseSessionDao.deleteInRange(start, end)
        if (sessions.isNotEmpty()) {
            exerciseSessionDao.upsertAll(sessions.map { it.toEntity() })
        }
    }

    override suspend fun exerciseInRange(start: Instant, end: Instant): List<ExerciseSession> =
        exerciseSessionDao.getInRange(start, end).map { it.toModel() }

    override suspend fun upsertHeartRate(samples: List<HeartRateSample>) {
        samples.mapNotNull { it.hcRecordId }.distinct().forEach { id ->
            heartRateSampleDao.deleteByHcRecordId(id)
        }
        if (samples.isNotEmpty()) {
            heartRateSampleDao.insertAll(samples.map { it.toEntity() })
        }
    }

    override suspend fun replaceHeartRate(
        start: Instant,
        end: Instant,
        samples: List<HeartRateSample>,
    ) {
        heartRateSampleDao.deleteInRange(start, end)
        if (samples.isNotEmpty()) {
            heartRateSampleDao.insertAll(samples.map { it.toEntity() })
        }
    }

    override suspend fun heartRateInRange(start: Instant, end: Instant): List<HeartRateSample> =
        heartRateSampleDao.getInRange(start, end).map { it.toModel() }

    override suspend fun mergeDailyMetrics(metrics: DailyMetrics) {
        dailyMetricsDao.upsertMerging(metrics.toEntity())
    }

    override suspend fun dailyMetricsInRange(
        start: LocalDate,
        endExclusive: LocalDate,
    ): List<DailyMetrics> = dailyMetricsDao.getInRange(start, endExclusive).map { it.toModel() }

    override suspend fun deleteByHcRecordId(hcRecordId: String): Boolean {
        val sleep = sleepSessionDao.deleteByHcRecordId(hcRecordId)
        val exercise = exerciseSessionDao.deleteByHcRecordId(hcRecordId)
        val heartRate = heartRateSampleDao.deleteByHcRecordId(hcRecordId)
        return sleep + exercise + heartRate > 0
    }

    override suspend fun upsertDailyScores(scores: DailyScores) {
        var entity = scoreDao.get(scores.date)
        scores.sleep?.let { entity = it.toEntity(entity) }
        scores.readiness?.let { entity = it.toEntity(entity) }
        scores.activity?.let { entity = it.toEntity(entity) }
        entity?.let { scoreDao.upsert(it) }
    }

    override suspend fun recordSyncAttempt(
        source: String,
        at: Instant,
        success: Boolean,
        error: String?,
    ) {
        val existing = syncStateDao.get(source)
        syncStateDao.upsert(
            SyncStateEntity(
                source = source,
                lastSuccessAt = if (success) at else existing?.lastSuccessAt,
                lastAttemptAt = at,
                lastError = error,
            ),
        )
    }
}
