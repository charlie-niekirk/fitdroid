package com.fitdroid.core.scoring

import com.fitdroid.core.model.ActivityScore
import com.fitdroid.core.model.DailyMetrics
import com.fitdroid.core.model.ExerciseSession
import com.fitdroid.core.model.HeartRateSample
import com.fitdroid.core.model.ReadinessScore
import com.fitdroid.core.model.SleepScore
import com.fitdroid.core.model.SleepSession
import java.time.LocalDate
import java.time.ZoneId

data class DailyScores(
    val date: LocalDate,
    val sleep: SleepScore?,
    val readiness: ReadinessScore?,
    val activity: ActivityScore?,
)

class ScoringEngine(
    private val goals: ScoringGoals = ScoringGoals.Default,
    private val zoneId: ZoneId = ZoneId.systemDefault(),
) {
    fun scoreDate(
        date: LocalDate,
        sleepSessions: List<SleepSession>,
        metrics: DailyMetrics?,
        metricsHistory: List<DailyMetrics>,
        exerciseMinutesByDate: Map<LocalDate, Int>,
        heartRateSamples: List<HeartRateSample>,
        previousSleepScore: Int? = null,
    ): DailyScores {
        val night = sleepSessions.nightsEndingOn(date, zoneId)
        val sleep = night?.let { SleepScorer.score(date, it, sleepSessions, goals, zoneId) }
        val sleepForReadiness = sleep?.score ?: previousSleepScore
        val exerciseMinutes = exerciseMinutesByDate[date] ?: metrics?.exerciseMinutes ?: 0
        val hasReadinessInputs = night != null || metrics != null || exerciseMinutes > 0
        val readiness = if (hasReadinessInputs) {
            ReadinessScorer.score(
                date = date,
                metrics = metrics,
                metricsHistory = metricsHistory,
                exerciseMinutesByDate = exerciseMinutesByDate,
                sleepScore = sleepForReadiness,
            )
        } else {
            null
        }
        val activity = ActivityScorer.score(
            date = date,
            metrics = metrics,
            exerciseMinutes = exerciseMinutes,
            heartRateSamples = heartRateSamples,
            goals = goals,
        )
        return DailyScores(
            date = date,
            sleep = sleep,
            readiness = readiness,
            activity = activity,
        )
    }

    fun scoreDates(
        dates: List<LocalDate>,
        sleepSessions: List<SleepSession>,
        metrics: List<DailyMetrics>,
        exerciseSessions: List<ExerciseSession>,
        heartRateSamples: List<HeartRateSample>,
    ): List<DailyScores> {
        val metricsByDate = metrics.associateBy { it.date }
        val exerciseByDate = exerciseMinutesByDate(exerciseSessions, zoneId)
        val heartRateByDate = heartRateSamples.groupBy { it.time.atZone(zoneId).toLocalDate() }
        return dates.sorted().map { date ->
            scoreDate(
                date = date,
                sleepSessions = sleepSessions,
                metrics = metricsByDate[date],
                metricsHistory = metrics,
                exerciseMinutesByDate = exerciseByDate,
                heartRateSamples = heartRateByDate[date].orEmpty(),
            )
        }
    }
}

internal fun exerciseMinutesByDate(
    sessions: List<ExerciseSession>,
    zoneId: ZoneId,
): Map<LocalDate, Int> {
    val minutes = mutableMapOf<LocalDate, Int>()
    for (session in sessions) {
        val date = session.start.atZone(zoneId).toLocalDate()
        minutes[date] = (minutes[date] ?: 0) + session.duration.toMinutes().toInt().coerceAtLeast(0)
    }
    return minutes
}
