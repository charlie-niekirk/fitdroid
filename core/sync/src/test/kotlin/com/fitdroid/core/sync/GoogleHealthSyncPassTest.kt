package com.fitdroid.core.sync

import com.fitdroid.core.common.AppError
import com.fitdroid.core.common.result.Result
import com.fitdroid.core.network.GoogleHealthClient
import com.fitdroid.core.network.GoogleHealthFeatureFlag
import com.fitdroid.core.network.HealthDataType
import com.fitdroid.core.network.model.DailyHeartRateVariability
import com.fitdroid.core.network.model.DataPoint
import com.fitdroid.core.network.model.GoogleDate
import com.fitdroid.core.network.model.IdentityResponse
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

class GoogleHealthSyncPassTest {
    private val zone = ZoneOffset.UTC
    private val clock = Clock.fixed(Instant.parse("2026-08-28T12:00:00Z"), zone)
    private val today = LocalDate.of(2026, 8, 28)

    @Test
    fun sync_skipsWhenFeatureFlagIsOff() = runTest {
        val pass = GoogleHealthSyncPass(
            client = FakeGoogleHealthClient(),
            featureFlag = GoogleHealthFeatureFlag { false },
            store = FakeLocalHealthStore(),
            preferences = FakeSyncPreferences(),
            clock = clock,
            zoneId = zone,
        )

        val result = pass.sync()

        assertTrue(result.isSkipped)
    }

    @Test
    fun sync_mergesDailyPointsAndAdvancesWatermark() = runTest {
        val store = FakeLocalHealthStore()
        val prefs = FakeSyncPreferences()
        val client = FakeGoogleHealthClient(
            points = mapOf(
                HealthDataType.DailyHeartRateVariability to listOf(
                    DataPoint(
                        dailyHeartRateVariability = DailyHeartRateVariability(
                            date = GoogleDate(2026, 8, 28),
                            deepSleepRootMeanSquareOfSuccessiveDifferencesMilliseconds = 42.0,
                        ),
                    ),
                ),
            ),
        )
        val pass = GoogleHealthSyncPass(
            client = client,
            featureFlag = GoogleHealthFeatureFlag { true },
            store = store,
            preferences = prefs,
            clock = clock,
            zoneId = zone,
        )

        val result = pass.sync()

        assertTrue(result.isSuccess)
        assertEquals(42.0, store.metrics[today]?.hrvRmssdMs)
        assertEquals(today, prefs.watermarks[HealthDataType.DailyHeartRateVariability])
        assertTrue(client.verified)
    }

    @Test
    fun sync_failsWhenLinkageCannotBeVerified() = runTest {
        val pass = GoogleHealthSyncPass(
            client = FakeGoogleHealthClient(linkageError = AppError.Network("nope")),
            featureFlag = GoogleHealthFeatureFlag { true },
            store = FakeLocalHealthStore(),
            preferences = FakeSyncPreferences(),
            clock = clock,
            zoneId = zone,
        )

        val result = pass.sync()

        assertTrue(result.isFailed)
        assertTrue(result.retryable)
    }
}

private class FakeGoogleHealthClient(
    private val points: Map<HealthDataType, List<DataPoint>> = emptyMap(),
    private val linkageError: AppError? = null,
) : GoogleHealthClient {
    var verified: Boolean = false
        private set

    override suspend fun verifyLinkage(): Result<IdentityResponse> {
        linkageError?.let { return Result.Failure(it) }
        verified = true
        return Result.Success(IdentityResponse(healthUserId = "user-1"))
    }

    override suspend fun listAllDataPoints(
        type: HealthDataType,
        startInclusive: LocalDate,
        endExclusive: LocalDate,
    ): Result<List<DataPoint>> = Result.Success(points[type].orEmpty())

    override suspend fun listAllDataPoints(
        type: HealthDataType,
        startInclusive: Instant,
        endExclusive: Instant,
    ): Result<List<DataPoint>> = Result.Success(emptyList())
}
