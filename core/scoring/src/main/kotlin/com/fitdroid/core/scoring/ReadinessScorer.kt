package com.fitdroid.core.scoring

import com.fitdroid.core.model.DailyMetrics
import com.fitdroid.core.model.ReadinessScore
import com.fitdroid.core.model.ReadinessScoreBreakdown
import java.time.LocalDate

internal object ReadinessScorer {
    private const val HrvWeight = 0.35
    private const val RhrWeight = 0.20
    private const val SleepWeight = 0.30
    private const val TrainingWeight = 0.15
    private const val DegradedRhrWeight = 0.35
    private const val DegradedSleepWeight = 0.50
    private const val DegradedTrainingWeight = 0.15
    private const val HrvHistoryDays = 30
    private const val AcuteDays = 7
    private const val ChronicDays = 28

    fun score(
        date: LocalDate,
        metrics: DailyMetrics?,
        metricsHistory: List<DailyMetrics>,
        exerciseMinutesByDate: Map<LocalDate, Int>,
        sleepScore: Int?,
    ): ReadinessScore {
        val hrv = hrvScore(metrics?.hrvRmssdMs, metricsHistory.hrvValues(before = date))
        val rhr = rhrScore(metrics?.restingHeartRateBpm, metricsHistory.rhrValues(before = date))
        val training = trainingLoadScore(date, exerciseMinutesByDate)
        val degraded = hrv == null
        val total = if (degraded) {
            weightedAverage(
                rhr?.let { it to DegradedRhrWeight },
                sleepScore?.let { it to DegradedSleepWeight },
                training?.let { it to DegradedTrainingWeight },
            )
        } else {
            weightedAverage(
                hrv?.let { it to HrvWeight },
                rhr?.let { it to RhrWeight },
                sleepScore?.let { it to SleepWeight },
                training?.let { it to TrainingWeight },
            )
        }
        return ReadinessScore(
            date = date,
            score = total,
            breakdown = ReadinessScoreBreakdown(
                hrv = hrv,
                restingHeartRate = rhr ?: 0,
                sleep = sleepScore ?: 0,
                trainingLoad = training,
            ),
            usingDegradedModel = degraded,
        )
    }

    internal fun hrvScore(current: Double?, history: List<Double>): Int? {
        if (current == null) return null
        val mean = Statistics.mean(history) ?: return 70
        val stdDev = Statistics.sampleStdDev(history)
        if (stdDev == null || stdDev == 0.0) {
            return if (current >= mean) 80 else 60
        }
        val z = Statistics.zScore(current, mean, stdDev) ?: return 70
        // Higher HRV than baseline is better. z=0 → 70, z=+1.2 → 100, z=-1.2 → 40.
        return (70.0 + z * 25.0).roundToScore()
    }

    internal fun rhrScore(current: Long?, history: List<Double>): Int? {
        if (current == null) return null
        val value = current.toDouble()
        val mean = Statistics.mean(history) ?: return 70
        val stdDev = Statistics.sampleStdDev(history)
        if (stdDev == null || stdDev == 0.0) {
            return if (value <= mean) 80 else 60
        }
        val z = Statistics.zScore(value, mean, stdDev) ?: return 70
        // Lower RHR than baseline is better.
        return (70.0 - z * 25.0).roundToScore()
    }

    internal fun trainingLoadScore(
        date: LocalDate,
        exerciseMinutesByDate: Map<LocalDate, Int>,
    ): Int? {
        if (exerciseMinutesByDate.isEmpty()) return null
        val acute = averageMinutes(date, AcuteDays, exerciseMinutesByDate)
        val chronic = averageMinutes(date, ChronicDays, exerciseMinutesByDate)
        if (chronic == 0.0) {
            return if (acute == 0.0) 70 else 80
        }
        val ratio = acute / chronic
        return when {
            ratio in 0.8..1.3 -> 100
            ratio < 0.8 -> lerp(ratio, 0.0, 0.8, 50.0, 100.0).roundToScore()
            ratio < 1.5 -> lerp(ratio, 1.3, 1.5, 100.0, 40.0).roundToScore()
            else -> 20
        }
    }

    private fun averageMinutes(
        date: LocalDate,
        days: Int,
        exerciseMinutesByDate: Map<LocalDate, Int>,
    ): Double {
        val start = date.minusDays((days - 1).toLong())
        var total = 0
        var cursor = start
        repeat(days) {
            total += exerciseMinutesByDate[cursor] ?: 0
            cursor = cursor.plusDays(1)
        }
        return total.toDouble() / days
    }

    private fun List<DailyMetrics>.hrvValues(before: LocalDate): List<Double> =
        filter { it.date.isBefore(before) && it.date >= before.minusDays(HrvHistoryDays.toLong()) }
            .mapNotNull { it.hrvRmssdMs }

    private fun List<DailyMetrics>.rhrValues(before: LocalDate): List<Double> =
        filter { it.date.isBefore(before) && it.date >= before.minusDays(HrvHistoryDays.toLong()) }
            .mapNotNull { it.restingHeartRateBpm?.toDouble() }
}
