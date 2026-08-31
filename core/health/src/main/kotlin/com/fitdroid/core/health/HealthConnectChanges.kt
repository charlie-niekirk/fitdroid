package com.fitdroid.core.health

import com.fitdroid.core.model.ExerciseSession
import com.fitdroid.core.model.HeartRateSample
import com.fitdroid.core.model.RestingHeartRateSample
import com.fitdroid.core.model.SleepSession
import java.time.Instant

sealed interface HealthRecordPayload {
    data class Sleep(val session: SleepSession) : HealthRecordPayload

    data class HeartRate(val samples: List<HeartRateSample>) : HealthRecordPayload

    data class RestingHeartRate(val sample: RestingHeartRateSample) : HealthRecordPayload

    data class Steps(
        val count: Long,
        val start: Instant,
        val end: Instant,
        val hcRecordId: String,
    ) : HealthRecordPayload

    data class Exercise(val session: ExerciseSession) : HealthRecordPayload

    data class Calories(
        val kcal: Double,
        val start: Instant,
        val end: Instant,
        val hcRecordId: String,
    ) : HealthRecordPayload

    data class Distance(
        val meters: Double,
        val start: Instant,
        val end: Instant,
        val hcRecordId: String,
    ) : HealthRecordPayload

    data class Unknown(val type: String, val hcRecordId: String) : HealthRecordPayload
}

sealed interface HealthConnectChange {
    data class Upsert(
        val recordId: String,
        val lastModified: Instant,
        val payload: HealthRecordPayload,
    ) : HealthConnectChange

    data class Deletion(val recordId: String) : HealthConnectChange
}

data class HealthConnectChangesPage(
    val changes: List<HealthConnectChange>,
    val nextToken: String,
    val hasMore: Boolean,
    val tokenExpired: Boolean,
)
