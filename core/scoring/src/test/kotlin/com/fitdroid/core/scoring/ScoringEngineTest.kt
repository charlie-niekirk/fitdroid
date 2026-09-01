package com.fitdroid.core.scoring

import com.fitdroid.core.model.DailyMetrics
import com.fitdroid.core.model.ExerciseSession
import com.fitdroid.core.model.SleepSession
import com.fitdroid.core.model.SleepStage
import com.fitdroid.core.model.SleepStageType
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ScoringEngineTest {
    private val zone = ZoneOffset.UTC
    private val engine = ScoringEngine(zoneId = zone)
    private val date = LocalDate.of(2026, 8, 28)

    @Test
    fun scoreDates_computesSleepThenReadinessThenActivityInChronologicalOrder() {
        val sleep = night(date)
        val metrics = DailyMetrics(
            date = date,
            steps = 10_000,
            restingHeartRateBpm = 58,
            hrvRmssdMs = 45.0,
            exerciseMinutes = 30,
        )
        val history = (1L..14L).map { offset ->
            DailyMetrics(
                date = date.minusDays(offset),
                restingHeartRateBpm = 60,
                hrvRmssdMs = 40.0,
            )
        }
        val exercise = ExerciseSession(
            id = "run",
            start = date.atTime(LocalTime.of(7, 0)).toInstant(zone),
            end = date.atTime(LocalTime.of(7, 30)).toInstant(zone),
            activityType = "running",
        )

        val scores = engine.scoreDates(
            dates = listOf(date),
            sleepSessions = listOf(sleep),
            metrics = history + metrics,
            exerciseSessions = listOf(exercise),
            heartRateSamples = emptyList(),
        )

        assertEquals(1, scores.size)
        assertTrue(scores[0].sleep != null)
        assertTrue(scores[0].readiness != null)
        assertTrue(scores[0].activity != null)
        assertTrue(!scores[0].readiness!!.usingDegradedModel)
        assertEquals(100, scores[0].activity!!.breakdown.steps)
        assertEquals(100, scores[0].activity!!.breakdown.activeMinutes)
    }

    @Test
    fun scoreDates_degradesReadinessWithoutHrv() {
        val sleep = night(date)
        val metrics = DailyMetrics(date = date, steps = 8_000, restingHeartRateBpm = 58)
        val scores = engine.scoreDates(
            dates = listOf(date),
            sleepSessions = listOf(sleep),
            metrics = listOf(metrics),
            exerciseSessions = emptyList(),
            heartRateSamples = emptyList(),
        )
        assertTrue(scores[0].readiness!!.usingDegradedModel)
        assertNull(scores[0].readiness!!.breakdown.hrv)
    }

    private fun night(wakeDate: LocalDate): SleepSession {
        val start = wakeDate.minusDays(1).atTime(LocalTime.of(22, 0)).toInstant(zone)
        val end = wakeDate.atTime(LocalTime.of(6, 0)).toInstant(zone)
        val restorativeStart = start.plusSeconds(4 * 3600)
        return SleepSession(
            id = "sleep-$wakeDate",
            start = start,
            end = end,
            stages = listOf(
                SleepStage(SleepStageType.Light, start, restorativeStart),
                SleepStage(SleepStageType.Deep, restorativeStart, restorativeStart.plusSeconds(2 * 3600)),
                SleepStage(SleepStageType.Rem, restorativeStart.plusSeconds(2 * 3600), end),
            ),
        )
    }
}
