package com.fitdroid.core.scoring

import com.fitdroid.core.model.DailyMetrics
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class ReadinessScorerTest {
    private val date = LocalDate.of(2026, 8, 28)

    @Test
    fun hrvScore_isNullWhenCurrentHrvMissing() {
        assertNull(ReadinessScorer.hrvScore(null, listOf(40.0, 42.0, 41.0)))
    }

    @Test
    fun hrvScore_rewardsHrvAboveBaseline() {
        val history = List(14) { 40.0 }
        val above = ReadinessScorer.hrvScore(50.0, history)
        val below = ReadinessScorer.hrvScore(30.0, history)
        assertTrue(above != null && below != null)
        assertTrue(above!! > below!!)
    }

    @Test
    fun rhrScore_rewardsHeartRateBelowBaseline() {
        val history = List(14) { 60.0 }
        val recovered = ReadinessScorer.rhrScore(54, history)
        val strained = ReadinessScorer.rhrScore(68, history)
        assertTrue(recovered != null && strained != null)
        assertTrue(recovered!! > strained!!)
    }

    @Test
    fun trainingLoadScore_is100InTheSweetSpot() {
        val minutes = (0L until 28L).associate { offset ->
            date.minusDays(offset) to 40
        }
        assertEquals(100, ReadinessScorer.trainingLoadScore(date, minutes))
    }

    @Test
    fun trainingLoadScore_dropsWhenAcuteLoadSpikes() {
        val minutes = mutableMapOf<LocalDate, Int>()
        repeat(28) { offset ->
            minutes[date.minusDays(offset.toLong())] = if (offset < 7) 120 else 20
        }
        val score = ReadinessScorer.trainingLoadScore(date, minutes)
        assertTrue(score != null && score < 50)
    }

    @Test
    fun score_usesDegradedModelWhenHrvIsMissing() {
        val metrics = DailyMetrics(date = date, restingHeartRateBpm = 58)
        val history = (1L..14L).map { offset ->
            DailyMetrics(
                date = date.minusDays(offset),
                restingHeartRateBpm = 60,
                hrvRmssdMs = 40.0,
            )
        }
        val result = ReadinessScorer.score(
            date = date,
            metrics = metrics,
            metricsHistory = history,
            exerciseMinutesByDate = emptyMap(),
            sleepScore = 80,
        )
        assertTrue(result.usingDegradedModel)
        assertNull(result.breakdown.hrv)
        assertEquals(80, result.breakdown.sleep)
        assertTrue(result.score in 1..100)
    }

    @Test
    fun score_includesHrvWhenGoogleHealthDataIsPresent() {
        val metrics = DailyMetrics(date = date, restingHeartRateBpm = 58, hrvRmssdMs = 45.0)
        val history = (1L..14L).map { offset ->
            DailyMetrics(
                date = date.minusDays(offset),
                restingHeartRateBpm = 60,
                hrvRmssdMs = 40.0,
            )
        }
        val result = ReadinessScorer.score(
            date = date,
            metrics = metrics,
            metricsHistory = history,
            exerciseMinutesByDate = emptyMap(),
            sleepScore = 80,
        )
        assertTrue(!result.usingDegradedModel)
        assertTrue(result.breakdown.hrv != null)
    }
}
