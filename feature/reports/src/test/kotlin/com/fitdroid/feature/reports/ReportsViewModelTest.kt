package com.fitdroid.feature.reports

import com.fitdroid.core.database.ScoreRepository
import com.fitdroid.core.designsystem.component.TrendDirection
import com.fitdroid.core.model.ActivityScore
import com.fitdroid.core.model.ActivityScoreBreakdown
import com.fitdroid.core.model.DailyScores
import com.fitdroid.core.model.ReadinessScore
import com.fitdroid.core.model.ReadinessScoreBreakdown
import com.fitdroid.core.model.SleepScore
import com.fitdroid.core.model.SleepScoreBreakdown
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.Locale
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.orbitmvi.orbit.test.testWithInternalState

class ReportsViewModelTest {
    private val zone = ZoneOffset.UTC
    private val today = LocalDate.of(2026, 9, 2)
    private val clock = Clock.fixed(today.atTime(12, 0).toInstant(zone), zone)
    private val locale = Locale.US

    @Test
    fun reportRange_currentWeekStartsOnMonday() {
        val range = reportRange(today, ReportPeriod.Week, 0)
        assertEquals(LocalDate.of(2026, 8, 31), range.start)
        assertEquals(LocalDate.of(2026, 9, 7), range.endExclusive)
    }

    @Test
    fun reportRange_previousMonthIsAugust() {
        val range = reportRange(today, ReportPeriod.Month, 1)
        assertEquals(LocalDate.of(2026, 8, 1), range.start)
        assertEquals(LocalDate.of(2026, 9, 1), range.endExclusive)
    }

    @Test
    fun toUiState_averagesCurrentWeekAndComparesPrevious() {
        val monday = LocalDate.of(2026, 8, 31)
        val previousMonday = LocalDate.of(2026, 8, 24)
        val ui = ReportsState(
            isLoading = false,
            period = ReportPeriod.Week,
            today = today,
            scores = listOf(
                daily(previousMonday, sleep = 70, readiness = 80, activity = 50),
                daily(monday, sleep = 80, readiness = 70, activity = 60),
                daily(monday.plusDays(1), sleep = 90, readiness = 80, activity = 80),
            ),
        ).toUiState(locale)

        assertTrue(ui.hasData)
        assertEquals("Aug 31 – Sep 6", ui.periodLabel)
        assertEquals(2, ui.daysWithScores)
        assertEquals(3, ui.daysInPeriod)
        assertEquals(85, ui.sleepAverage)
        assertEquals(75, ui.readinessAverage)
        assertEquals(70, ui.activityAverage)
        assertEquals(15, ui.sleepDelta)
        assertEquals(TrendDirection.Up, ui.sleepTrendDirection)
        assertEquals(-5, ui.readinessDelta)
        assertEquals(TrendDirection.Down, ui.readinessTrendDirection)
        assertEquals(5, ui.sleepComponents.size)
        assertEquals(ReportComponent.Duration, ui.sleepComponents[0].key)
        assertEquals(80, ui.sleepComponents[0].score)
        assertTrue(ui.canGoPrevious)
        assertFalse(ui.canGoNext)
    }

    @Test
    fun toUiState_whenEmpty_hasNoData() {
        val ui = ReportsState(
            isLoading = false,
            today = today,
        ).toUiState(locale)
        assertFalse(ui.hasData)
        assertEquals(null, ui.sleepAverage)
    }

    @Test
    fun selectPreviousPeriod_incrementsOffset() = runTest {
        val viewModel = ReportsViewModel(FakeScoreRepository(), clock, zone)
        viewModel.testWithInternalState(
            this,
            ReportsState(isLoading = false, today = today),
        ) {
            containerHost.selectPreviousPeriod()
            expectInternalState { copy(offset = 1) }
            cancelAndIgnoreRemainingItems()
        }
    }

    @Test
    fun selectPeriod_resetsOffset() = runTest {
        val viewModel = ReportsViewModel(FakeScoreRepository(), clock, zone)
        viewModel.testWithInternalState(
            this,
            ReportsState(isLoading = false, today = today, offset = 3, period = ReportPeriod.Week),
        ) {
            containerHost.selectPeriod(ReportPeriod.Month)
            expectInternalState { copy(period = ReportPeriod.Month, offset = 0) }
            cancelAndIgnoreRemainingItems()
        }
    }

    private fun daily(
        date: LocalDate,
        sleep: Int,
        readiness: Int,
        activity: Int,
    ): DailyScores = DailyScores(
        date = date,
        sleep = SleepScore(date, sleep, SleepScoreBreakdown(80, 80, 80, 80, 80)),
        readiness = ReadinessScore(
            date = date,
            score = readiness,
            breakdown = ReadinessScoreBreakdown(null, 70, sleep, 50),
            usingDegradedModel = true,
        ),
        activity = ActivityScore(date, activity, ActivityScoreBreakdown(60, 50, 40)),
    )
}

private class FakeScoreRepository(vararg scores: DailyScores) : ScoreRepository {
    private val flow = MutableStateFlow(scores.toList())
    override fun observeInRange(start: LocalDate, endExclusive: LocalDate): Flow<List<DailyScores>> = flow
}
