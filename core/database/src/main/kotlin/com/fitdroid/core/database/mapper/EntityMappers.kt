package com.fitdroid.core.database.mapper

import com.fitdroid.core.database.entity.DailyMetricsEntity
import com.fitdroid.core.database.entity.ExerciseSessionEntity
import com.fitdroid.core.database.entity.HeartRateSampleEntity
import com.fitdroid.core.database.entity.ScoreEntity
import com.fitdroid.core.database.entity.SleepSessionEntity
import com.fitdroid.core.database.entity.SleepSessionWithStages
import com.fitdroid.core.database.entity.SleepStageEntity
import com.fitdroid.core.database.entity.SyncStateEntity
import com.fitdroid.core.model.ActivityScore
import com.fitdroid.core.model.ActivityScoreBreakdown
import com.fitdroid.core.model.DailyMetrics
import com.fitdroid.core.model.DailyScores
import com.fitdroid.core.model.ExerciseSession
import com.fitdroid.core.model.HeartRateSample
import com.fitdroid.core.model.ReadinessScore
import com.fitdroid.core.model.ReadinessScoreBreakdown
import com.fitdroid.core.model.SleepScore
import com.fitdroid.core.model.SleepScoreBreakdown
import com.fitdroid.core.model.SleepSession
import com.fitdroid.core.model.SleepStage
import com.fitdroid.core.model.SyncState

fun SleepSession.toEntity(): SleepSessionEntity =
    SleepSessionEntity(
        id = id,
        hcRecordId = hcRecordId,
        hcLastModified = hcLastModified,
        start = start,
        end = end,
        notes = notes,
    )

fun SleepStage.toEntity(sessionId: String): SleepStageEntity =
    SleepStageEntity(
        sessionId = sessionId,
        type = type,
        start = start,
        end = end,
    )

fun SleepSession.stageEntities(): List<SleepStageEntity> = stages.map { it.toEntity(id) }

fun SleepSessionWithStages.toModel(): SleepSession =
    SleepSession(
        id = session.id,
        hcRecordId = session.hcRecordId,
        hcLastModified = session.hcLastModified,
        start = session.start,
        end = session.end,
        stages = stages.map { it.toModel() },
        notes = session.notes,
    )

fun SleepStageEntity.toModel(): SleepStage =
    SleepStage(type = type, start = start, end = end)

fun DailyMetrics.toEntity(): DailyMetricsEntity =
    DailyMetricsEntity(
        date = date,
        restingHeartRateBpm = restingHeartRateBpm,
        hrvRmssdMs = hrvRmssdMs,
        spo2Percent = spo2Percent,
        respiratoryRateBrpm = respiratoryRateBrpm,
        skinTempDeviationCelsius = skinTempDeviationCelsius,
        steps = steps,
        caloriesKcal = caloriesKcal,
        distanceMeters = distanceMeters,
        exerciseMinutes = exerciseMinutes,
    )

fun DailyMetricsEntity.toModel(): DailyMetrics =
    DailyMetrics(
        date = date,
        restingHeartRateBpm = restingHeartRateBpm,
        hrvRmssdMs = hrvRmssdMs,
        spo2Percent = spo2Percent,
        respiratoryRateBrpm = respiratoryRateBrpm,
        skinTempDeviationCelsius = skinTempDeviationCelsius,
        steps = steps,
        caloriesKcal = caloriesKcal,
        distanceMeters = distanceMeters,
        exerciseMinutes = exerciseMinutes,
    )

fun ExerciseSession.toEntity(): ExerciseSessionEntity =
    ExerciseSessionEntity(
        id = id,
        hcRecordId = hcRecordId,
        start = start,
        end = end,
        activityType = activityType,
        caloriesKcal = caloriesKcal,
    )

fun ExerciseSessionEntity.toModel(): ExerciseSession =
    ExerciseSession(
        id = id,
        hcRecordId = hcRecordId,
        start = start,
        end = end,
        activityType = activityType,
        caloriesKcal = caloriesKcal,
    )

fun HeartRateSample.toEntity(sleepSessionId: String? = null): HeartRateSampleEntity =
    HeartRateSampleEntity(
        timestamp = time,
        bpm = bpm,
        resolutionSeconds = resolutionSeconds,
        hcRecordId = hcRecordId,
        sleepSessionId = sleepSessionId,
    )

fun HeartRateSampleEntity.toModel(): HeartRateSample =
    HeartRateSample(
        time = timestamp,
        bpm = bpm,
        hcRecordId = hcRecordId,
        resolutionSeconds = resolutionSeconds,
    )

fun SleepScore.toEntity(existing: ScoreEntity? = null): ScoreEntity =
    (existing ?: ScoreEntity(date = date)).copy(
        sleepScore = score,
        sleepDuration = breakdown.duration,
        sleepRestorative = breakdown.restorative,
        sleepEfficiency = breakdown.efficiency,
        sleepDisturbances = breakdown.disturbances,
        sleepConsistency = breakdown.consistency,
    )

fun ReadinessScore.toEntity(existing: ScoreEntity? = null): ScoreEntity =
    (existing ?: ScoreEntity(date = date)).copy(
        readinessScore = score,
        readinessHrv = breakdown.hrv,
        readinessRestingHeartRate = breakdown.restingHeartRate,
        readinessSleep = breakdown.sleep,
        readinessTrainingLoad = breakdown.trainingLoad,
        readinessDegraded = usingDegradedModel,
    )

fun ActivityScore.toEntity(existing: ScoreEntity? = null): ScoreEntity =
    (existing ?: ScoreEntity(date = date)).copy(
        activityScore = score,
        activitySteps = breakdown.steps,
        activityActiveMinutes = breakdown.activeMinutes,
        activityCardioLoad = breakdown.cardioLoad,
    )

fun ScoreEntity.toSleepScore(): SleepScore? {
    val value = sleepScore ?: return null
    return SleepScore(
        date = date,
        score = value,
        breakdown = SleepScoreBreakdown(
            duration = sleepDuration ?: 0,
            restorative = sleepRestorative ?: 0,
            efficiency = sleepEfficiency ?: 0,
            disturbances = sleepDisturbances ?: 0,
            consistency = sleepConsistency ?: 0,
        ),
    )
}

fun ScoreEntity.toReadinessScore(): ReadinessScore? {
    val value = readinessScore ?: return null
    return ReadinessScore(
        date = date,
        score = value,
        breakdown = ReadinessScoreBreakdown(
            hrv = readinessHrv,
            restingHeartRate = readinessRestingHeartRate ?: 0,
            sleep = readinessSleep ?: 0,
            trainingLoad = readinessTrainingLoad,
        ),
        usingDegradedModel = readinessDegraded,
    )
}

fun ScoreEntity.toDailyScores(): DailyScores =
    DailyScores(
        date = date,
        sleep = toSleepScore(),
        readiness = toReadinessScore(),
        activity = toActivityScore(),
    )

fun ScoreEntity.toActivityScore(): ActivityScore? {
    val value = activityScore ?: return null
    return ActivityScore(
        date = date,
        score = value,
        breakdown = ActivityScoreBreakdown(
            steps = activitySteps ?: 0,
            activeMinutes = activityActiveMinutes ?: 0,
            cardioLoad = activityCardioLoad ?: 0,
        ),
    )
}

fun SyncState.toEntity(): SyncStateEntity =
    SyncStateEntity(
        source = source,
        lastSuccessAt = lastSuccessAt,
        lastAttemptAt = lastAttemptAt,
        lastError = lastError,
    )

fun SyncStateEntity.toModel(): SyncState =
    SyncState(
        source = source,
        lastSuccessAt = lastSuccessAt,
        lastAttemptAt = lastAttemptAt,
        lastError = lastError,
    )
