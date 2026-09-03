package com.fitdroid.feature.sleep

import com.fitdroid.core.database.ScoreRepository
import com.fitdroid.core.database.SleepRepository
import com.fitdroid.core.designsystem.component.TrendDirection
import com.fitdroid.core.model.DailyScores
import com.fitdroid.core.model.SleepScore
import com.fitdroid.core.model.SleepScoreBreakdown
import com.fitdroid.core.model.SleepSession
import com.fitdroid.core.model.SleepStage
import com.fitdroid.core.model.SleepStageType
import com.fitdroid.core.model.UserSettings
import com.fitdroid.core.sync.ImmediateSync
import com.fitdroid.core.sync.UserSettingsRepository
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
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

class SleepViewModelTest {
    private val zone = ZoneOffset.UTC
    private val today = LocalDate.of(2026, 9, 1)
    private val clock = Clock.fixed(today.atTime(12, 0).toInstant(zone), zone)
    private val locale = Locale.US

    @Test
    fun toUiState_mapsNightSessionAndBreakdown() {
        val night = nightEndingOn(today)
        val score = SleepScore(
            date = today,
            score = 84,
            breakdown = SleepScoreBreakdown(82, 90, 86, 70, 75),
        )
        val ui = SleepState(
            isLoading = false,
            selectedDate = today,
            today = today,
            sessions = listOf(night),
            scores = listOf(score, score.copy(date = today.minusDays(1), score = 80)),
        ).toUiState(zone, locale)

        assertTrue(ui.hasNight)
        assertEquals(84, ui.score)
        assertEquals("6h 0m", ui.asleepLabel)
        assertEquals(5, ui.components.size)
        assertEquals(SleepComponent.Duration, ui.components[0].key)
        assertEquals(4, ui.trendDelta)
        assertEquals(TrendDirection.Up, ui.trendDirection)
        assertFalse(ui.canGoNext)
        assertTrue(ui.canGoPrevious)
        assertEquals(4, ui.hypnogram.size)
        assertEquals("2:30 AM", ui.midpointLabel)
        assertEquals(0f, ui.hypnogram.first().startFraction, 0.001f)
        assertEquals(1f / 7f, ui.hypnogram.first().endFraction, 0.001f)
    }

    @Test
    fun toUiState_whenNoSession_hasEmptyNight() {
        val ui = SleepState(
            isLoading = false,
            selectedDate = today,
            today = today,
        ).toUiState(zone, locale)
        assertFalse(ui.hasNight)
        assertEquals(null, ui.score)
    }

    @Test
    fun toUiState_passesThroughClassicHypnogramSetting() {
        val ui = SleepState(
            isLoading = false,
            selectedDate = today,
            today = today,
            useClassicHypnogram = true,
        ).toUiState(zone, locale)

        assertTrue(ui.useClassicHypnogram)
    }

    @Test
    fun toUiState_projectsShortInteriorAwakeStagesAsRestlessness() {
        val ui = SleepState(
            isLoading = false,
            selectedDate = today,
            today = today,
            sessions = listOf(restlessnessSession(today)),
        ).toUiState(zone, locale)

        assertEquals("8m", ui.awakeDuration)
        assertEquals("2m", ui.restlessnessDuration)
        assertEquals("12m", ui.lightDuration)
        assertEquals("35m", ui.asleepLabel)
        assertEquals("10m", ui.classicAwakeDuration)
        assertEquals("10m", ui.classicLightDuration)
        assertEquals(6, ui.stagedHypnogram.size)
        assertEquals(
            Duration.ofMinutes(2),
            ui.stagedHypnogram
                .filter { it.type == SleepStageType.AwakeInBed }
                .fold(Duration.ZERO) { total, segment -> total + segment.duration },
        )
    }

    @Test
    fun toUiState_smoothsShortVisualInterruptionsWithoutChangingDurations() {
        val start = today.atStartOfDay().toInstant(zone)
        val session = SleepSession(
            id = "rapid-transitions",
            start = start,
            end = start.plusSeconds(20 * 60),
            stages = listOf(
                SleepStage(SleepStageType.Rem, start, start.plusSeconds(60)),
                SleepStage(
                    SleepStageType.AwakeInBed,
                    start.plusSeconds(60),
                    start.plusSeconds(2 * 60),
                ),
                SleepStage(
                    SleepStageType.Rem,
                    start.plusSeconds(2 * 60),
                    start.plusSeconds(3 * 60),
                ),
                SleepStage(
                    SleepStageType.Light,
                    start.plusSeconds(3 * 60),
                    start.plusSeconds(4 * 60),
                ),
                SleepStage(
                    SleepStageType.Rem,
                    start.plusSeconds(4 * 60),
                    start.plusSeconds(20 * 60),
                ),
            ),
        )

        val ui = SleepState(
            isLoading = false,
            selectedDate = today,
            today = today,
            sessions = listOf(session),
        ).toUiState(zone, locale)

        assertEquals("18m", ui.remDuration)
        assertEquals("2m", ui.lightDuration)
        assertEquals("1m", ui.restlessnessDuration)
        assertEquals(2, ui.stagedHypnogram.size)
        assertEquals(
            Duration.ofMinutes(20),
            ui.stagedHypnogram.single { it.type == SleepStageType.Rem }.duration,
        )
    }

    @Test
    fun selectPreviousNight_movesSelectedDate() = runTest {
        val viewModel = SleepViewModel(
            FakeSleepRepository(),
            FakeScoreRepository(),
            FakeUserSettingsRepository(),
            FakeImmediateSync(),
            clock,
            zone,
        )
        viewModel.testWithInternalState(this, SleepState(isLoading = false, selectedDate = today, today = today)) {
            containerHost.selectPreviousNight()
            expectInternalState { copy(selectedDate = today.minusDays(1)) }
            cancelAndIgnoreRemainingItems()
        }
    }

    @Test
    fun refresh_requestsImmediateSync() = runTest {
        val sync = FakeImmediateSync()
        val viewModel = SleepViewModel(
            FakeSleepRepository(),
            FakeScoreRepository(),
            FakeUserSettingsRepository(),
            sync,
            clock,
            zone,
        )
        viewModel.testWithInternalState(this, SleepState(isLoading = false, selectedDate = today, today = today)) {
            containerHost.refresh()
            expectInternalState { copy(isRefreshing = true) }
            cancelAndIgnoreRemainingItems()
        }
        assertEquals(1, sync.requests)
    }
}

private fun restlessnessSession(wakeDate: LocalDate): SleepSession {
    val start = wakeDate.atStartOfDay().toInstant(ZoneOffset.UTC)
    return SleepSession(
        id = "restless-night",
        start = start,
        end = start.plusSeconds(43 * 60),
        stages = listOf(
            SleepStage(SleepStageType.Awake, start, start.plusSeconds(6 * 60)),
            SleepStage(
                SleepStageType.Light,
                start.plusSeconds(6 * 60),
                start.plusSeconds(11 * 60),
            ),
            SleepStage(
                SleepStageType.Awake,
                start.plusSeconds(11 * 60),
                start.plusSeconds(12 * 60),
            ),
            SleepStage(
                SleepStageType.Light,
                start.plusSeconds(12 * 60),
                start.plusSeconds(16 * 60),
            ),
            SleepStage(
                SleepStageType.AwakeInBed,
                start.plusSeconds(16 * 60),
                start.plusSeconds(17 * 60),
            ),
            SleepStage(
                SleepStageType.Light,
                start.plusSeconds(17 * 60),
                start.plusSeconds(18 * 60),
            ),
            SleepStage(
                SleepStageType.Deep,
                start.plusSeconds(18 * 60),
                start.plusSeconds(41 * 60),
            ),
            SleepStage(
                SleepStageType.Awake,
                start.plusSeconds(41 * 60),
                start.plusSeconds(43 * 60),
            ),
        ),
    )
}

private fun nightEndingOn(wakeDate: LocalDate): SleepSession {
    val zone = ZoneOffset.UTC
    val start = wakeDate.minusDays(1).atTime(LocalTime.of(23, 0)).toInstant(zone)
    val end = wakeDate.atTime(LocalTime.of(6, 0)).toInstant(zone)
    val deepStart = start.plusSeconds(3600)
    val remStart = deepStart.plusSeconds(3600)
    val lightEnd = remStart.plusSeconds(5 * 3600)
    return SleepSession(
        id = "night",
        start = start,
        end = end,
        stages = listOf(
            SleepStage(SleepStageType.Awake, start, deepStart),
            SleepStage(SleepStageType.Deep, deepStart, remStart),
            SleepStage(SleepStageType.Rem, remStart, remStart.plusSeconds(3600)),
            SleepStage(SleepStageType.Light, remStart.plusSeconds(3600), lightEnd),
        ),
    )
}

private class FakeSleepRepository(vararg sessions: SleepSession) : SleepRepository {
    private val flow = MutableStateFlow(sessions.toList())
    override fun observeInRange(start: Instant, end: Instant): Flow<List<SleepSession>> = flow
}

private class FakeScoreRepository(vararg scores: DailyScores) : ScoreRepository {
    private val flow = MutableStateFlow(scores.toList())
    override fun observeInRange(start: LocalDate, endExclusive: LocalDate): Flow<List<DailyScores>> = flow
}

private class FakeImmediateSync : ImmediateSync {
    var requests = 0
    override fun request() {
        requests++
    }
}

private class FakeUserSettingsRepository : UserSettingsRepository {
    override val settings = MutableStateFlow(UserSettings.Default)

    override suspend fun update(transform: (UserSettings) -> UserSettings) {
        settings.value = transform(settings.value)
    }
}
