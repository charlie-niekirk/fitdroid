package com.fitdroid.core.scoring

import com.fitdroid.core.model.SleepSession
import com.fitdroid.core.model.SleepStage
import com.fitdroid.core.model.SleepStageType
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SleepScorerTest {
    private val zone = ZoneOffset.UTC
    private val date = LocalDate.of(2026, 8, 28)

    @Test
    fun durationScore_is100AtTargetAndScalesLinearlyBelow() {
        val target = Duration.ofHours(8)
        assertEquals(100, SleepScorer.durationScore(Duration.ofHours(8), target))
        assertEquals(50, SleepScorer.durationScore(Duration.ofHours(4), target))
        assertEquals(100, SleepScorer.durationScore(Duration.ofHours(10), target))
        assertEquals(70, SleepScorer.durationScore(Duration.ofHours(14), target))
    }

    @Test
    fun restorativeScore_is100AtFortyPercentDeepPlusRem() {
        val night = night(
            asleepHours = 8.0,
            restorativeHours = 3.2,
        )
        assertEquals(100, SleepScorer.restorativeScore(night))
    }

    @Test
    fun efficiencyScore_is100WhenAsleepIsNinetyPercentOfTimeInBed() {
        val start = date.atTime(LocalTime.of(22, 0)).toInstant(zone)
        val end = start.plus(Duration.ofHours(8))
        val night = NightSleep(
            start = start,
            end = end,
            stages = listOf(
                SleepStage(SleepStageType.Light, start, start.plus(Duration.ofMinutes(48))),
                SleepStage(SleepStageType.Deep, start.plus(Duration.ofMinutes(48)), end),
            ),
        )
        // 8h in bed, 8h asleep but first 0? Wait both are asleep types. Need awake padding.
        // Rebuild: 8h in bed, 7.2h asleep = 90% efficiency → 100.
        val awakeEnd = start.plus(Duration.ofMinutes(48))
        val efficient = NightSleep(
            start = start,
            end = end,
            stages = listOf(
                SleepStage(SleepStageType.Awake, start, awakeEnd),
                SleepStage(SleepStageType.Light, awakeEnd, end),
            ),
        )
        assertEquals(100, SleepScorer.efficiencyScore(efficient))
    }

    @Test
    fun disturbanceScore_dropsWithMemorableAwakenings() {
        assertEquals(100, SleepScorer.disturbanceScore(0))
        assertEquals(85, SleepScorer.disturbanceScore(1))
        assertEquals(70, SleepScorer.disturbanceScore(2))
        assertEquals(20, SleepScorer.disturbanceScore(6))
    }

    @Test
    fun memorableAwakenings_ignoreSleepLatencyAndCountMidSleepWakesOverFiveMinutes() {
        val start = Instant.parse("2026-08-27T22:00:00Z")
        val end = Instant.parse("2026-08-28T06:00:00Z")
        val night = NightSleep(
            start = start,
            end = end,
            stages = listOf(
                SleepStage(SleepStageType.Awake, start, start.plus(Duration.ofMinutes(20))),
                SleepStage(
                    SleepStageType.Light,
                    start.plus(Duration.ofMinutes(20)),
                    Instant.parse("2026-08-28T02:00:00Z"),
                ),
                SleepStage(
                    SleepStageType.Awake,
                    Instant.parse("2026-08-28T02:00:00Z"),
                    Instant.parse("2026-08-28T02:12:00Z"),
                ),
                SleepStage(SleepStageType.Rem, Instant.parse("2026-08-28T02:12:00Z"), end),
            ),
        )
        assertEquals(1, night.memorableAwakenings())
    }

    @Test
    fun consistencyScore_is100WhenBedtimesAreStable() {
        val sessions = (0L..5L).map { offset ->
            sessionEndingOn(date.minusDays(offset), bedtimeHour = 22)
        }
        assertEquals(100, SleepScorer.consistencyScore(sessions, date, zone))
    }

    @Test
    fun consistencyScore_isNeutralWithSparseHistory() {
        val sessions = listOf(sessionEndingOn(date, bedtimeHour = 22))
        assertEquals(70, SleepScorer.consistencyScore(sessions, date, zone))
    }

    @Test
    fun score_weightsComponentsIntoOverall() {
        val night = night(asleepHours = 8.0, restorativeHours = 3.2)
        val sessions = (0L..5L).map { sessionEndingOn(date.minusDays(it), bedtimeHour = 22) }
        val result = SleepScorer.score(date, night, sessions, ScoringGoals.Default, zone)
        assertEquals(100, result.breakdown.duration)
        assertEquals(100, result.breakdown.restorative)
        assertTrue(result.score in 90..100)
    }

    private fun night(asleepHours: Double, restorativeHours: Double): NightSleep {
        val start = date.minusDays(1).atTime(LocalTime.of(22, 0)).toInstant(zone)
        val asleep = Duration.ofMinutes((asleepHours * 60).toLong())
        val restorative = Duration.ofMinutes((restorativeHours * 60).toLong())
        val light = asleep.minus(restorative)
        val deepEnd = start.plus(light)
        val end = start.plus(asleep)
        return NightSleep(
            start = start,
            end = end,
            stages = listOf(
                SleepStage(SleepStageType.Light, start, deepEnd),
                SleepStage(SleepStageType.Deep, deepEnd, end),
            ),
        )
    }

    private fun sessionEndingOn(wakeDate: LocalDate, bedtimeHour: Int): SleepSession {
        val end = wakeDate.atTime(LocalTime.of(6, 0)).toInstant(zone)
        val start = wakeDate.minusDays(1).atTime(LocalTime.of(bedtimeHour, 0)).toInstant(zone)
        return SleepSession(
            id = wakeDate.toString(),
            start = start,
            end = end,
            stages = listOf(SleepStage(SleepStageType.Light, start, end)),
        )
    }
}
