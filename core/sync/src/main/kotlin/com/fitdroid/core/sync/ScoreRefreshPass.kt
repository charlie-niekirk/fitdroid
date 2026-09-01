package com.fitdroid.core.sync

import com.fitdroid.core.scoring.ScoringEngine
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId

@SingleIn(AppScope::class)
@Inject
class ScoreRefreshPass(
    private val store: LocalHealthStore,
    private val engine: ScoringEngine,
    private val clock: Clock,
    private val zoneId: ZoneId,
) {
    suspend fun refresh() {
        val today = LocalDate.now(clock.withZone(zoneId))
        val scoreStart = today.minusDays(ScoreDays - 1)
        val sleepStart = scoreStart.minusDays(SleepHistoryPaddingDays).atStartOfDay(zoneId).toInstant()
        val metricsStart = scoreStart.minusDays(MetricsHistoryPaddingDays)
        val exerciseStart = scoreStart.minusDays(ExerciseHistoryPaddingDays).atStartOfDay(zoneId).toInstant()
        val end = today.plusDays(1).atStartOfDay(zoneId).toInstant()
        val dates = generateSequence(scoreStart) { it.plusDays(1) }
            .takeWhile { !it.isAfter(today) }
            .toList()
        val scores = engine.scoreDates(
            dates = dates,
            sleepSessions = store.sleepInRange(sleepStart, end),
            metrics = store.dailyMetricsInRange(metricsStart, today.plusDays(1)),
            exerciseSessions = store.exerciseInRange(exerciseStart, end),
            heartRateSamples = store.heartRateInRange(scoreStart.atStartOfDay(zoneId).toInstant(), end),
        )
        scores.forEach { store.upsertDailyScores(it) }
    }

    companion object {
        const val ScoreDays = 30L
        const val SleepHistoryPaddingDays = 14L
        const val MetricsHistoryPaddingDays = 30L
        const val ExerciseHistoryPaddingDays = 28L
    }
}
