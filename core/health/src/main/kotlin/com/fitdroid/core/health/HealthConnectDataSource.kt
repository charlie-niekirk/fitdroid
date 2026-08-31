package com.fitdroid.core.health

import com.fitdroid.core.common.result.Result
import com.fitdroid.core.model.ExerciseSession
import com.fitdroid.core.model.HeartRateSample
import com.fitdroid.core.model.RestingHeartRateSample
import com.fitdroid.core.model.SleepSession
import java.time.Instant

interface HealthConnectDataSource {
    fun availability(): HealthConnectAvailability

    fun requiredPermissions(): Set<String>

    suspend fun grantedPermissions(): Result<Set<String>>

    suspend fun sleepSessions(start: Instant, end: Instant): Result<List<SleepSession>>

    suspend fun heartRate(start: Instant, end: Instant): Result<List<HeartRateSample>>

    suspend fun restingHeartRate(start: Instant, end: Instant): Result<List<RestingHeartRateSample>>

    suspend fun stepsTotal(start: Instant, end: Instant): Result<Long>

    suspend fun exerciseSessions(start: Instant, end: Instant): Result<List<ExerciseSession>>

    suspend fun caloriesKcal(start: Instant, end: Instant): Result<Double>

    suspend fun distanceMeters(start: Instant, end: Instant): Result<Double>

    suspend fun newChangesToken(): Result<String>

    suspend fun changes(token: String): Result<HealthConnectChangesPage>
}
