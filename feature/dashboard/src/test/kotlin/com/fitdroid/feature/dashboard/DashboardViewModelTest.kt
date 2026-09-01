package com.fitdroid.feature.dashboard

import com.fitdroid.core.database.ActivityRepository
import com.fitdroid.core.database.ScoreRepository
import com.fitdroid.core.model.ActivityScore
import com.fitdroid.core.model.ActivityScoreBreakdown
import com.fitdroid.core.model.DailyMetrics
import com.fitdroid.core.model.DailyScores
import com.fitdroid.core.model.ExerciseSession
import com.fitdroid.core.model.SleepScore
import com.fitdroid.core.model.SleepScoreBreakdown
import com.fitdroid.core.sync.ImmediateSync
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.orbitmvi.orbit.test.testWithInternalState

class DashboardViewModelTest {
    private val zone = ZoneOffset.UTC
    private val today = LocalDate.of(2026, 9, 1)
    private val clock = Clock.fixed(today.atTime(12, 0).toInstant(zone), zone)

    @Test
    fun collect_whenScoresPresent_showsTodayRingsAndTrend() = runTest {
        val sleep = SleepScore(
            date = today,
            score = 84,
            breakdown = SleepScoreBreakdown(80, 90, 85, 70, 75),
        )
        val activity = ActivityScore(
            date = today,
            score = 68,
            breakdown = ActivityScoreBreakdown(70, 60, 50),
        )
        val yesterday = sleep.copy(date = today.minusDays(1), score = 70)
        val scores = FakeScoreRepository(
            DailyScores(date = today.minusDays(1), sleep = yesterday, readiness = null, activity = null),
            DailyScores(date = today, sleep = sleep, readiness = null, activity = activity),
        )
        val metrics = FakeActivityRepository(
            DailyMetrics(date = today, steps = 8_420),
        )
        val sync = FakeImmediateSync()
        val viewModel = DashboardViewModel(scores, metrics, sync, clock, zone)

        viewModel.testWithInternalState(this) {
            runOnCreate()
            expectInternalState {
                copy(
                    isLoading = false,
                    isRefreshing = false,
                    today = today,
                    sleepScore = sleep,
                    activityScore = activity,
                    sleepTrend = listOf(70f, 84f),
                    steps = 8_420,
                )
            }
            cancelAndIgnoreRemainingItems()
        }
        assertEquals(1, sync.requests)
        assertTrue(viewModel.container.stateFlow.value.hasAnyScore)
    }

    @Test
    fun collect_whenEmpty_showsEmptyDashboard() = runTest {
        val viewModel = DashboardViewModel(
            FakeScoreRepository(),
            FakeActivityRepository(),
            FakeImmediateSync(),
            clock,
            zone,
        )
        viewModel.testWithInternalState(this) {
            runOnCreate()
            expectInternalState { copy(isLoading = false, today = today) }
            cancelAndIgnoreRemainingItems()
        }
        assertFalse(viewModel.container.stateFlow.value.hasAnyScore)
    }

    @Test
    fun onSleepClick_postsOpenSleep() = runTest {
        val viewModel = DashboardViewModel(
            FakeScoreRepository(),
            FakeActivityRepository(),
            FakeImmediateSync(),
            clock,
            zone,
        )
        viewModel.testWithInternalState(this, DashboardState(isLoading = false, today = today)) {
            containerHost.onSleepClick()
            expectSideEffect(DashboardEffect.OpenSleep)
        }
    }

    @Test
    fun refresh_requestsImmediateSync() = runTest {
        val sync = FakeImmediateSync()
        val viewModel = DashboardViewModel(
            FakeScoreRepository(),
            FakeActivityRepository(),
            sync,
            clock,
            zone,
        )
        viewModel.testWithInternalState(this, DashboardState(isLoading = false, today = today)) {
            containerHost.refresh()
            expectInternalState { copy(isRefreshing = true) }
            cancelAndIgnoreRemainingItems()
        }
        assertEquals(1, sync.requests)
    }
}

private class FakeScoreRepository(vararg scores: DailyScores) : ScoreRepository {
    private val flow = MutableStateFlow(scores.toList())
    override fun observeInRange(start: LocalDate, endExclusive: LocalDate): Flow<List<DailyScores>> = flow
}

private class FakeActivityRepository(
    vararg metrics: DailyMetrics,
) : ActivityRepository {
    private val metricsFlow = MutableStateFlow(metrics.toList())
    override fun observeMetrics(start: LocalDate, endExclusive: LocalDate): Flow<List<DailyMetrics>> = metricsFlow
    override fun observeExercise(start: Instant, end: Instant): Flow<List<ExerciseSession>> =
        MutableStateFlow(emptyList())
}

private class FakeImmediateSync : ImmediateSync {
    var requests = 0
    override fun request() {
        requests++
    }
}
