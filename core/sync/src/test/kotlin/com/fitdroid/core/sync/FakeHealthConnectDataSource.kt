package com.fitdroid.core.sync

import com.fitdroid.core.common.result.Result
import com.fitdroid.core.health.HealthConnectAvailability
import com.fitdroid.core.health.HealthConnectChangesPage
import com.fitdroid.core.health.HealthConnectDataSource
import com.fitdroid.core.model.ExerciseSession
import com.fitdroid.core.model.HeartRateSample
import com.fitdroid.core.model.RestingHeartRateSample
import com.fitdroid.core.model.SleepSession
import java.time.Instant

internal class FakeHealthConnectDataSource : HealthConnectDataSource {
    var availability: HealthConnectAvailability = HealthConnectAvailability.Available
    var granted: Set<String> = emptySet()
    var nextToken: String = "token-1"
    var sleep: List<SleepSession> = emptyList()
    var heartRate: List<HeartRateSample> = emptyList()
    var restingHeartRate: List<RestingHeartRateSample> = emptyList()
    var steps: Long = 0
    var exercise: List<ExerciseSession> = emptyList()
    var calories: Double = 0.0
    var distance: Double = 0.0
    var changesPages: MutableList<HealthConnectChangesPage> = mutableListOf()
    var newTokenCalls: Int = 0
    var changesCalls: Int = 0

    override fun availability(): HealthConnectAvailability = availability

    override fun requiredPermissions(): Set<String> = emptySet()

    override suspend fun grantedPermissions(): Result<Set<String>> = Result.Success(granted)

    override suspend fun sleepSessions(start: Instant, end: Instant): Result<List<SleepSession>> =
        Result.Success(sleep.filter { it.start < end && it.end > start })

    override suspend fun heartRate(start: Instant, end: Instant): Result<List<HeartRateSample>> =
        Result.Success(heartRate.filter { it.time >= start && it.time < end })

    override suspend fun restingHeartRate(
        start: Instant,
        end: Instant,
    ): Result<List<RestingHeartRateSample>> =
        Result.Success(restingHeartRate.filter { it.time >= start && it.time < end })

    override suspend fun stepsTotal(start: Instant, end: Instant): Result<Long> = Result.Success(steps)

    override suspend fun exerciseSessions(
        start: Instant,
        end: Instant,
    ): Result<List<ExerciseSession>> =
        Result.Success(exercise.filter { it.start < end && it.end > start })

    override suspend fun caloriesKcal(start: Instant, end: Instant): Result<Double> =
        Result.Success(calories)

    override suspend fun distanceMeters(start: Instant, end: Instant): Result<Double> =
        Result.Success(distance)

    override suspend fun newChangesToken(): Result<String> {
        newTokenCalls++
        return Result.Success(nextToken)
    }

    override suspend fun changes(token: String): Result<HealthConnectChangesPage> {
        changesCalls++
        val page = changesPages.removeFirstOrNull()
            ?: HealthConnectChangesPage(
                changes = emptyList(),
                nextToken = token,
                hasMore = false,
                tokenExpired = false,
            )
        return Result.Success(page)
    }
}
