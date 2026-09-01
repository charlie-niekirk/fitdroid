package com.fitdroid.core.scoring

import com.fitdroid.core.model.ActivityScore
import com.fitdroid.core.model.ActivityScoreBreakdown
import com.fitdroid.core.model.DailyMetrics
import com.fitdroid.core.model.HeartRateSample
import java.time.LocalDate

internal object ActivityScorer {
    private const val StepsWeight = 0.50
    private const val ActiveMinutesWeight = 0.30
    private const val CardioWeight = 0.20

    fun score(
        date: LocalDate,
        metrics: DailyMetrics?,
        exerciseMinutes: Int,
        heartRateSamples: List<HeartRateSample>,
        goals: ScoringGoals,
    ): ActivityScore? {
        val steps = stepsScore(metrics?.steps, goals.steps)
        val active = activeMinutesScore(exerciseMinutes, goals.activeMinutes)
        val cardioMinutes = HeartRateZones.cardioMinutes(heartRateSamples)
        val cardio = if (heartRateSamples.isEmpty()) {
            null
        } else {
            ratioScore(cardioMinutes, goals.cardioMinutes.toDouble())
        }
        if (steps == null && exerciseMinutes <= 0 && heartRateSamples.isEmpty()) {
            return null
        }
        val total = weightedAverage(
            steps?.let { it to StepsWeight },
            active to ActiveMinutesWeight,
            cardio?.let { it to CardioWeight },
        )
        return ActivityScore(
            date = date,
            score = total,
            breakdown = ActivityScoreBreakdown(
                steps = steps ?: 0,
                activeMinutes = active,
                cardioLoad = cardio ?: 0,
            ),
        )
    }

    internal fun stepsScore(steps: Long?, goal: Long): Int? {
        if (steps == null) return null
        return ratioScore(steps.toDouble(), goal.toDouble())
    }

    internal fun activeMinutesScore(minutes: Int, goal: Int): Int =
        ratioScore(minutes.toDouble(), goal.toDouble())

    private fun ratioScore(value: Double, goal: Double): Int {
        if (goal <= 0.0) return 0
        return (value / goal * 100.0).roundToScore()
    }
}
