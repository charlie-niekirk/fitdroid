package com.fitdroid.feature.activity

import com.fitdroid.core.database.ActivityRepository
import com.fitdroid.core.database.ScoreRepository
import com.fitdroid.core.model.ActivityScore
import com.fitdroid.core.model.ActivityScoreBreakdown
import com.fitdroid.core.model.DailyMetrics
import com.fitdroid.core.model.DailyScores
import com.fitdroid.core.model.ExerciseSession
import com.fitdroid.core.model.UserSettings
import com.fitdroid.core.sync.ImmediateSync
import com.fitdroid.core.sync.UserSettingsRepository
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.orbitmvi.orbit.test.testWithInternalState

class ActivityViewModelTest {
    private val zone = ZoneOffset.UTC
    private val today = LocalDate.of(2026, 9, 1)
    private val clock = Clock.fixed(today.atTime(12, 0).toInstant(zone), zone)

    @Test
    fun collect_projectsSelectedDayMetricsAndWorkouts() = runTest {
        val score = ActivityScore(
            date = today,
            score = 68,
            breakdown = ActivityScoreBreakdown(70, 60, 50),
        )
        val workout = ExerciseSession(
            id = "run",
            start = today.atTime(LocalTime.of(7, 0)).toInstant(zone),
            end = today.atTime(LocalTime.of(7, 32)).toInstant(zone),
            activityType = "running",
            caloriesKcal = 280.0,
        )
        val viewModel = ActivityViewModel(
            FakeActivityRepository(
                metrics = listOf(DailyMetrics(date = today, steps = 8_420, exerciseMinutes = 32)),
                exercises = listOf(workout),
            ),
            FakeScoreRepository(DailyScores(today, sleep = null, readiness = null, activity = score)),
            FakeUserSettingsRepository(),
            FakeImmediateSync(),
            clock,
            zone,
        )
        viewModel.testWithInternalState(this) {
            runOnCreate()
            expectInternalState {
                copy(
                    isLoading = false,
                    selectedDate = today,
                    today = today,
                    scoresByDate = mapOf(today to score),
                    metricsByDate = mapOf(today to DailyMetrics(date = today, steps = 8_420, exerciseMinutes = 32)),
                    exercisesByDate = mapOf(today to listOf(workout)),
                    recentScores = listOf(68f),
                )
            }
            cancelAndIgnoreRemainingItems()
        }
        assertTrue(viewModel.container.stateFlow.value.hasDay)
        assertEquals(8_420L, viewModel.container.stateFlow.value.metrics?.steps)
    }

    @Test
    fun selectPreviousDay_movesSelectedDate() = runTest {
        val viewModel = ActivityViewModel(
            FakeActivityRepository(),
            FakeScoreRepository(),
            FakeUserSettingsRepository(),
            FakeImmediateSync(),
            clock,
            zone,
        )
        viewModel.testWithInternalState(this, ActivityState(isLoading = false, selectedDate = today, today = today)) {
            containerHost.selectPreviousDay()
            expectInternalState { copy(selectedDate = today.minusDays(1)) }
            cancelAndIgnoreRemainingItems()
        }
    }

    @Test
    fun refresh_requestsImmediateSync() = runTest {
        val sync = FakeImmediateSync()
        val viewModel = ActivityViewModel(
            FakeActivityRepository(),
            FakeScoreRepository(),
            FakeUserSettingsRepository(),
            sync,
            clock,
            zone,
        )
        viewModel.testWithInternalState(this, ActivityState(isLoading = false, selectedDate = today, today = today)) {
            containerHost.refresh()
            expectInternalState { copy(isRefreshing = true) }
            cancelAndIgnoreRemainingItems()
        }
        assertEquals(1, sync.requests)
    }
}

private class FakeActivityRepository(
    metrics: List<DailyMetrics> = emptyList(),
    exercises: List<ExerciseSession> = emptyList(),
) : ActivityRepository {
    private val metricsFlow = MutableStateFlow(metrics)
    private val exerciseFlow = MutableStateFlow(exercises)
    override fun observeMetrics(start: LocalDate, endExclusive: LocalDate): Flow<List<DailyMetrics>> = metricsFlow
    override fun observeExercise(start: Instant, end: Instant): Flow<List<ExerciseSession>> = exerciseFlow
}

private class FakeScoreRepository(vararg scores: DailyScores) : ScoreRepository {
    private val flow = MutableStateFlow(scores.toList())
    override fun observeInRange(start: LocalDate, endExclusive: LocalDate): Flow<List<DailyScores>> = flow
}

private class FakeUserSettingsRepository : UserSettingsRepository {
    override val settings = MutableStateFlow(UserSettings.Default)
    override suspend fun update(transform: (UserSettings) -> UserSettings) {
        settings.value = transform(settings.value)
    }
}

private class FakeImmediateSync : ImmediateSync {
    var requests = 0
    override fun request() {
        requests++
    }
}
