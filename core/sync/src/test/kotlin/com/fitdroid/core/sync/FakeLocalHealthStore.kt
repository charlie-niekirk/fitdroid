package com.fitdroid.core.sync

import com.fitdroid.core.model.DailyMetrics
import com.fitdroid.core.model.DailyScores
import com.fitdroid.core.model.ExerciseSession
import com.fitdroid.core.model.HeartRateSample
import com.fitdroid.core.model.SleepSession
import com.fitdroid.core.model.SyncState
import java.time.Instant
import java.time.LocalDate

internal class FakeLocalHealthStore : LocalHealthStore {
    val sleep = mutableListOf<SleepSession>()
    val exercise = mutableListOf<ExerciseSession>()
    val heartRate = mutableListOf<HeartRateSample>()
    val metrics = mutableMapOf<LocalDate, DailyMetrics>()
    val scores = mutableMapOf<LocalDate, DailyScores>()
    val syncAttempts = mutableListOf<SyncState>()

    override suspend fun upsertSleep(session: SleepSession) {
        sleep.removeAll { it.id == session.id || it.hcRecordId == session.hcRecordId }
        sleep += session
    }

    override suspend fun replaceSleep(start: Instant, end: Instant, sessions: List<SleepSession>) {
        sleep.removeAll { it.start in start..<end }
        sleep += sessions
    }

    override suspend fun sleepInRange(start: Instant, end: Instant): List<SleepSession> =
        sleep.filter { it.start in start..<end }

    override suspend fun upsertExercise(session: ExerciseSession) {
        exercise.removeAll { it.id == session.id || it.hcRecordId == session.hcRecordId }
        exercise += session
    }

    override suspend fun replaceExercise(
        start: Instant,
        end: Instant,
        sessions: List<ExerciseSession>,
    ) {
        exercise.removeAll { it.start in start..<end }
        exercise += sessions
    }

    override suspend fun exerciseInRange(start: Instant, end: Instant): List<ExerciseSession> =
        exercise.filter { it.start in start..<end }

    override suspend fun upsertHeartRate(samples: List<HeartRateSample>) {
        samples.mapNotNull { it.hcRecordId }.distinct().forEach { id ->
            heartRate.removeAll { it.hcRecordId == id }
        }
        heartRate += samples
    }

    override suspend fun replaceHeartRate(
        start: Instant,
        end: Instant,
        samples: List<HeartRateSample>,
    ) {
        heartRate.removeAll { it.time in start..<end }
        heartRate += samples
    }

    override suspend fun heartRateInRange(start: Instant, end: Instant): List<HeartRateSample> =
        heartRate.filter { it.time in start..<end }

    override suspend fun mergeDailyMetrics(incoming: DailyMetrics) {
        val existing = metrics[incoming.date]
        metrics[incoming.date] = DailyMetrics(
            date = incoming.date,
            restingHeartRateBpm = incoming.restingHeartRateBpm ?: existing?.restingHeartRateBpm,
            hrvRmssdMs = incoming.hrvRmssdMs ?: existing?.hrvRmssdMs,
            spo2Percent = incoming.spo2Percent ?: existing?.spo2Percent,
            respiratoryRateBrpm = incoming.respiratoryRateBrpm ?: existing?.respiratoryRateBrpm,
            skinTempDeviationCelsius = incoming.skinTempDeviationCelsius
                ?: existing?.skinTempDeviationCelsius,
            steps = incoming.steps ?: existing?.steps,
            caloriesKcal = incoming.caloriesKcal ?: existing?.caloriesKcal,
            distanceMeters = incoming.distanceMeters ?: existing?.distanceMeters,
            exerciseMinutes = incoming.exerciseMinutes ?: existing?.exerciseMinutes,
        )
    }

    override suspend fun dailyMetricsInRange(
        start: LocalDate,
        endExclusive: LocalDate,
    ): List<DailyMetrics> = metrics.values.filter { it.date in start..<endExclusive }

    override suspend fun deleteByHcRecordId(hcRecordId: String): Boolean {
        val sleepRemoved = sleep.removeAll { it.hcRecordId == hcRecordId }
        val exerciseRemoved = exercise.removeAll { it.hcRecordId == hcRecordId }
        val hrRemoved = heartRate.removeAll { it.hcRecordId == hcRecordId }
        return sleepRemoved || exerciseRemoved || hrRemoved
    }

    override suspend fun upsertDailyScores(value: DailyScores) {
        scores[value.date] = value
    }

    override suspend fun recordSyncAttempt(
        source: String,
        at: Instant,
        success: Boolean,
        error: String?,
    ) {
        val existing = syncAttempts.lastOrNull { it.source == source }
        syncAttempts += SyncState(
            source = source,
            lastSuccessAt = if (success) at else existing?.lastSuccessAt,
            lastAttemptAt = at,
            lastError = error,
        )
    }
}
