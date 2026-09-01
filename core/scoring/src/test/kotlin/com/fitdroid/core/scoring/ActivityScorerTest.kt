package com.fitdroid.core.scoring

import com.fitdroid.core.model.DailyMetrics
import com.fitdroid.core.model.HeartRateSample
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

class ActivityScorerTest {
    private val date = LocalDate.of(2026, 8, 28)

    @Test
    fun stepsScore_is100AtGoal() {
        assertEquals(100, ActivityScorer.stepsScore(10_000, 10_000))
        assertEquals(50, ActivityScorer.stepsScore(5_000, 10_000))
        assertNull(ActivityScorer.stepsScore(null, 10_000))
    }

    @Test
    fun activeMinutesScore_capsAt100() {
        assertEquals(100, ActivityScorer.activeMinutesScore(30, 30))
        assertEquals(100, ActivityScorer.activeMinutesScore(90, 30))
        assertEquals(50, ActivityScorer.activeMinutesScore(15, 30))
    }

    @Test
    fun score_combinesStepsActiveMinutesAndCardioLoad() {
        val samples = List(22) { index ->
            HeartRateSample(
                time = Instant.parse("2026-08-28T12:00:00Z").plusSeconds(index * 60L),
                bpm = 140,
                resolutionSeconds = 60,
            )
        }
        val result = ActivityScorer.score(
            date = date,
            metrics = DailyMetrics(date = date, steps = 10_000),
            exerciseMinutes = 30,
            heartRateSamples = samples,
            goals = ScoringGoals.Default,
        )
        assertTrue(result != null)
        assertEquals(100, result!!.breakdown.steps)
        assertEquals(100, result.breakdown.activeMinutes)
        assertEquals(100, result.breakdown.cardioLoad)
        assertEquals(100, result.score)
    }

    @Test
    fun score_isNullWhenThereIsNoActivityData() {
        assertNull(
            ActivityScorer.score(
                date = date,
                metrics = null,
                exerciseMinutes = 0,
                heartRateSamples = emptyList(),
                goals = ScoringGoals.Default,
            ),
        )
    }
}
