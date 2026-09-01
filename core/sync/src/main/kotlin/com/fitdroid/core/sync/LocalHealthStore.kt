package com.fitdroid.core.sync

import com.fitdroid.core.model.DailyMetrics
import com.fitdroid.core.model.ExerciseSession
import com.fitdroid.core.model.HeartRateSample
import com.fitdroid.core.model.SleepSession
import com.fitdroid.core.scoring.DailyScores
import java.time.Instant
import java.time.LocalDate

interface LocalHealthStore {
    suspend fun upsertSleep(session: SleepSession)

    suspend fun replaceSleep(start: Instant, end: Instant, sessions: List<SleepSession>)

    suspend fun sleepInRange(start: Instant, end: Instant): List<SleepSession>

    suspend fun upsertExercise(session: ExerciseSession)

    suspend fun replaceExercise(start: Instant, end: Instant, sessions: List<ExerciseSession>)

    suspend fun exerciseInRange(start: Instant, end: Instant): List<ExerciseSession>

    suspend fun upsertHeartRate(samples: List<HeartRateSample>)

    suspend fun replaceHeartRate(start: Instant, end: Instant, samples: List<HeartRateSample>)

    suspend fun heartRateInRange(start: Instant, end: Instant): List<HeartRateSample>

    suspend fun mergeDailyMetrics(metrics: DailyMetrics)

    suspend fun dailyMetricsInRange(start: LocalDate, endExclusive: LocalDate): List<DailyMetrics>

    suspend fun deleteByHcRecordId(hcRecordId: String): Boolean

    suspend fun upsertDailyScores(scores: DailyScores)

    suspend fun recordSyncAttempt(
        source: String,
        at: Instant,
        success: Boolean,
        error: String?,
    )
}
