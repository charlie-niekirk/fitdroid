package com.fitdroid.feature.onboarding

import com.fitdroid.core.common.result.Result
import com.fitdroid.core.health.HealthConnectAvailability
import com.fitdroid.core.health.HealthConnectChangesPage
import com.fitdroid.core.health.HealthConnectDataSource
import com.fitdroid.core.health.HealthConnectPermissions
import com.fitdroid.core.model.ExerciseSession
import com.fitdroid.core.model.HeartRateSample
import com.fitdroid.core.model.RestingHeartRateSample
import com.fitdroid.core.model.SleepSession
import java.time.Instant

internal class FakeOnboardingHealthConnect(
    private val availability: HealthConnectAvailability = HealthConnectAvailability.Available,
    private val required: Set<String> = HealthConnectPermissions.recordReadPermissions,
    var granted: Set<String> = emptySet(),
) : HealthConnectDataSource {
    override fun availability(): HealthConnectAvailability = availability

    override fun requiredPermissions(): Set<String> = required

    override suspend fun grantedPermissions(): Result<Set<String>> = Result.Success(granted)

    override suspend fun sleepSessions(start: Instant, end: Instant): Result<List<SleepSession>> =
        Result.Success(emptyList())

    override suspend fun heartRate(start: Instant, end: Instant): Result<List<HeartRateSample>> =
        Result.Success(emptyList())

    override suspend fun restingHeartRate(
        start: Instant,
        end: Instant,
    ): Result<List<RestingHeartRateSample>> = Result.Success(emptyList())

    override suspend fun stepsTotal(start: Instant, end: Instant): Result<Long> = Result.Success(0)

    override suspend fun exerciseSessions(
        start: Instant,
        end: Instant,
    ): Result<List<ExerciseSession>> = Result.Success(emptyList())

    override suspend fun caloriesKcal(start: Instant, end: Instant): Result<Double> = Result.Success(0.0)

    override suspend fun distanceMeters(start: Instant, end: Instant): Result<Double> = Result.Success(0.0)

    override suspend fun newChangesToken(): Result<String> = Result.Success("token")

    override suspend fun changes(token: String): Result<HealthConnectChangesPage> =
        Result.Success(
            HealthConnectChangesPage(
                changes = emptyList(),
                nextToken = token,
                hasMore = false,
                tokenExpired = false,
            ),
        )
}
