package com.fitdroid.core.health

import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.RestingHeartRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import com.fitdroid.core.model.ExerciseSession
import com.fitdroid.core.model.HeartRateSample
import com.fitdroid.core.model.RestingHeartRateSample
import com.fitdroid.core.model.SleepSession
import com.fitdroid.core.model.SleepStage
import com.fitdroid.core.model.SleepStageType

internal fun SleepSessionRecord.toSleepSession(): SleepSession =
    SleepSession(
        id = metadata.id,
        hcRecordId = metadata.id,
        hcLastModified = metadata.lastModifiedTime,
        start = startTime,
        end = endTime,
        stages = stages.map { it.toSleepStage() },
        notes = notes,
    )

internal fun SleepSessionRecord.Stage.toSleepStage(): SleepStage =
    SleepStage(
        type = sleepStageType(stage),
        start = startTime,
        end = endTime,
    )

internal fun sleepStageType(hcStage: Int): SleepStageType =
    when (hcStage) {
        SleepSessionRecord.STAGE_TYPE_AWAKE,
        SleepSessionRecord.STAGE_TYPE_AWAKE_IN_BED,
        SleepSessionRecord.STAGE_TYPE_OUT_OF_BED,
        -> SleepStageType.Awake

        SleepSessionRecord.STAGE_TYPE_LIGHT,
        SleepSessionRecord.STAGE_TYPE_SLEEPING,
        -> SleepStageType.Light

        SleepSessionRecord.STAGE_TYPE_DEEP -> SleepStageType.Deep

        SleepSessionRecord.STAGE_TYPE_REM -> SleepStageType.Rem

        else -> SleepStageType.Unknown
    }

internal fun HeartRateRecord.toHeartRateSamples(): List<HeartRateSample> =
    samples.map { sample ->
        HeartRateSample(
            time = sample.time,
            bpm = sample.beatsPerMinute,
            hcRecordId = metadata.id,
        )
    }

internal fun RestingHeartRateRecord.toRestingHeartRateSample(): RestingHeartRateSample =
    RestingHeartRateSample(
        time = time,
        bpm = beatsPerMinute,
        hcRecordId = metadata.id,
    )

internal fun ExerciseSessionRecord.toExerciseSession(): ExerciseSession =
    ExerciseSession(
        id = metadata.id,
        hcRecordId = metadata.id,
        start = startTime,
        end = endTime,
        activityType = exerciseTypeName(exerciseType),
    )

internal fun exerciseTypeName(type: Int): String =
    when (type) {
        ExerciseSessionRecord.EXERCISE_TYPE_RUNNING -> "running"
        ExerciseSessionRecord.EXERCISE_TYPE_WALKING -> "walking"
        ExerciseSessionRecord.EXERCISE_TYPE_BIKING -> "biking"
        ExerciseSessionRecord.EXERCISE_TYPE_HIKING -> "hiking"
        ExerciseSessionRecord.EXERCISE_TYPE_STRENGTH_TRAINING -> "strength_training"
        ExerciseSessionRecord.EXERCISE_TYPE_WEIGHTLIFTING -> "weightlifting"
        ExerciseSessionRecord.EXERCISE_TYPE_YOGA -> "yoga"
        ExerciseSessionRecord.EXERCISE_TYPE_SWIMMING_POOL -> "swimming_pool"
        ExerciseSessionRecord.EXERCISE_TYPE_SWIMMING_OPEN_WATER -> "swimming_open_water"
        ExerciseSessionRecord.EXERCISE_TYPE_ELLIPTICAL -> "elliptical"
        ExerciseSessionRecord.EXERCISE_TYPE_CALISTHENICS -> "calisthenics"
        ExerciseSessionRecord.EXERCISE_TYPE_STRETCHING -> "stretching"
        ExerciseSessionRecord.EXERCISE_TYPE_OTHER_WORKOUT -> "other_workout"
        else -> "unknown_$type"
    }

internal fun StepsRecord.toPayload(): HealthRecordPayload.Steps =
    HealthRecordPayload.Steps(
        count = count,
        start = startTime,
        end = endTime,
        hcRecordId = metadata.id,
    )

internal fun TotalCaloriesBurnedRecord.toPayload(): HealthRecordPayload.Calories =
    HealthRecordPayload.Calories(
        kcal = energy.inKilocalories,
        start = startTime,
        end = endTime,
        hcRecordId = metadata.id,
    )

internal fun DistanceRecord.toPayload(): HealthRecordPayload.Distance =
    HealthRecordPayload.Distance(
        meters = distance.inMeters,
        start = startTime,
        end = endTime,
        hcRecordId = metadata.id,
    )
