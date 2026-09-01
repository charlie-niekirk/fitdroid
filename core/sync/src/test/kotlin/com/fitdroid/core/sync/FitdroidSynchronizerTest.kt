package com.fitdroid.core.sync

import com.fitdroid.core.model.DailyMetrics
import com.fitdroid.core.model.SleepSession
import com.fitdroid.core.model.SleepStage
import com.fitdroid.core.model.SleepStageType
import com.fitdroid.core.network.GoogleHealthClient
import com.fitdroid.core.network.GoogleHealthFeatureFlag
import com.fitdroid.core.network.HealthDataType
import com.fitdroid.core.network.model.DataPoint
import com.fitdroid.core.network.model.IdentityResponse
import com.fitdroid.core.scoring.ScoringEngine
import com.fitdroid.core.common.result.Result
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset

class FitdroidSynchronizerTest {
    private val zone = ZoneOffset.UTC
    private val clock = Clock.fixed(Instant.parse("2026-08-28T12:00:00Z"), zone)
    private val date = LocalDate.of(2026, 8, 28)

    @Test
    fun sync_runsHealthConnectThenScoresEvenWhenGoogleHealthIsDisabled() = runTest {
        val store = FakeLocalHealthStore()
        val prefs = FakeSyncPreferences()
        val dataSource = FakeHealthConnectDataSource().apply {
            nextToken = "t1"
            sleep = listOf(night(date))
            steps = 10_000
        }
        val synchronizer = FitdroidSynchronizer(
            healthConnectPass = HealthConnectSyncPass(dataSource, store, prefs, clock, zone),
            googleHealthPass = GoogleHealthSyncPass(
                client = UnusedGoogleHealthClient,
                featureFlag = { false },
                store = store,
                preferences = prefs,
                clock = clock,
                zoneId = zone,
            ),
            scoreRefreshPass = ScoreRefreshPass(store, ScoringEngine(zoneId = zone), clock, zone),
            store = store,
            clock = clock,
        )

        val outcome = synchronizer.sync()

        assertTrue(outcome.healthConnect.isSuccess)
        assertTrue(outcome.googleHealth.isSkipped)
        assertTrue(!outcome.retry)
        assertTrue(store.scores[date]?.sleep != null)
        assertEquals(10_000L, store.metrics[date]?.steps)
        assertEquals(2, store.syncAttempts.size)
    }

    @Test
    fun scoreRefresh_writesComponentBreakdowns() = runTest {
        val store = FakeLocalHealthStore().apply {
            sleep += night(date)
            metrics[date] = DailyMetrics(date = date, steps = 5_000, restingHeartRateBpm = 58)
        }
        val pass = ScoreRefreshPass(store, ScoringEngine(zoneId = zone), clock, zone)

        pass.refresh()

        val scores = store.scores[date]
        assertTrue(scores?.sleep != null)
        assertTrue(scores?.readiness != null)
        assertTrue(scores?.readiness!!.usingDegradedModel)
        assertTrue(scores.activity != null)
        assertEquals(50, scores.activity!!.breakdown.steps)
    }

    private fun night(wakeDate: LocalDate): SleepSession {
        val start = wakeDate.minusDays(1).atTime(LocalTime.of(22, 0)).toInstant(zone)
        val end = wakeDate.atTime(LocalTime.of(6, 0)).toInstant(zone)
        return SleepSession(
            id = "sleep-$wakeDate",
            hcRecordId = "sleep-$wakeDate",
            start = start,
            end = end,
            stages = listOf(
                SleepStage(SleepStageType.Light, start, start.plusSeconds(4 * 3600)),
                SleepStage(SleepStageType.Deep, start.plusSeconds(4 * 3600), end),
            ),
        )
    }
}

private object UnusedGoogleHealthClient : GoogleHealthClient {
    override suspend fun verifyLinkage() = Result.Success(IdentityResponse(healthUserId = "x"))

    override suspend fun listAllDataPoints(
        type: HealthDataType,
        startInclusive: LocalDate,
        endExclusive: LocalDate,
    ) = Result.Success(emptyList<DataPoint>())

    override suspend fun listAllDataPoints(
        type: HealthDataType,
        startInclusive: Instant,
        endExclusive: Instant,
    ) = Result.Success(emptyList<DataPoint>())
}
