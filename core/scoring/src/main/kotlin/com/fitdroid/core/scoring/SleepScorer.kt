package com.fitdroid.core.scoring

import com.fitdroid.core.model.SleepScore
import com.fitdroid.core.model.SleepScoreBreakdown
import com.fitdroid.core.model.SleepSession
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import kotlin.math.sqrt

internal object SleepScorer {
    private const val DurationWeight = 0.30
    private const val RestorativeWeight = 0.25
    private const val EfficiencyWeight = 0.20
    private const val DisturbanceWeight = 0.15
    private const val ConsistencyWeight = 0.10
    private const val IdealRestorativeFraction = 0.40
    private const val IdealEfficiency = 0.90
    private const val ConsistencyNeutralMinutes = 30.0
    private const val ConsistencyWorstMinutes = 180.0

    fun score(
        date: LocalDate,
        night: NightSleep,
        recentSessions: List<SleepSession>,
        goals: ScoringGoals,
        zoneId: ZoneId,
    ): SleepScore {
        val duration = durationScore(night.asleep, goals.sleepTarget)
        val restorative = restorativeScore(night)
        val efficiency = efficiencyScore(night)
        val disturbances = disturbanceScore(night.memorableAwakenings())
        val consistency = consistencyScore(recentSessions, date, zoneId)
        val total = weightedAverage(
            duration to DurationWeight,
            restorative to RestorativeWeight,
            efficiency to EfficiencyWeight,
            disturbances to DisturbanceWeight,
            consistency to ConsistencyWeight,
        )
        return SleepScore(
            date = date,
            score = total,
            breakdown = SleepScoreBreakdown(
                duration = duration,
                restorative = restorative,
                efficiency = efficiency,
                disturbances = disturbances,
                consistency = consistency,
            ),
        )
    }

    internal fun durationScore(asleep: Duration, target: Duration): Int {
        val targetMinutes = target.toMinutes().coerceAtLeast(1).toDouble()
        val asleepMinutes = asleep.toMinutes().coerceAtLeast(0).toDouble()
        val ratio = asleepMinutes / targetMinutes
        return when {
            ratio <= 1.0 -> (ratio * 100.0).roundToScore()
            ratio <= 1.25 -> 100
            else -> lerp(ratio, 1.25, 1.75, 100.0, 70.0).roundToScore()
        }
    }

    internal fun restorativeScore(night: NightSleep): Int {
        val asleepMinutes = night.asleep.toMinutes()
        if (asleepMinutes <= 0) return 0
        val fraction = night.restorative.toMinutes().toDouble() / asleepMinutes
        return (fraction / IdealRestorativeFraction * 100.0).roundToScore()
    }

    internal fun efficiencyScore(night: NightSleep): Int {
        val inBed = night.timeInBed.toMinutes()
        if (inBed <= 0) return 0
        val efficiency = night.asleep.toMinutes().toDouble() / inBed
        return (efficiency / IdealEfficiency * 100.0).roundToScore()
    }

    internal fun disturbanceScore(awakenings: Int): Int =
        when {
            awakenings <= 0 -> 100
            awakenings == 1 -> 85
            awakenings == 2 -> 70
            awakenings == 3 -> 55
            awakenings == 4 -> 40
            else -> 20
        }

    internal fun consistencyScore(
        recentSessions: List<SleepSession>,
        date: LocalDate,
        zoneId: ZoneId,
    ): Int {
        val windowStart = date.minusDays(13)
        val bedtimes = recentSessions
            .filter { session ->
                val wake = session.wakeDate(zoneId)
                !wake.isBefore(windowStart) && !wake.isAfter(date)
            }
            .groupBy { it.wakeDate(zoneId) }
            .map { (_, nights) -> nights.minOf { it.start } }
            .map { bedtimeMinutesFromDusk(it, zoneId) }
        if (bedtimes.size < 3) return 70
        val stdDev = bedtimeStdDev(bedtimes) ?: return 70
        return when {
            stdDev <= ConsistencyNeutralMinutes -> 100

            stdDev >= ConsistencyWorstMinutes -> 0

            else -> lerp(
                stdDev,
                ConsistencyNeutralMinutes,
                ConsistencyWorstMinutes,
                100.0,
                0.0,
            ).roundToScore()
        }
    }

    private fun bedtimeMinutesFromDusk(start: Instant, zoneId: ZoneId): Double {
        val local = start.atZone(zoneId).toLocalTime()
        var minutes = local.toSecondOfDay() / 60.0
        // Treat times before noon as the following morning so 11pm and 1am are close.
        if (local.isBefore(LocalTime.NOON)) minutes += 24 * 60
        return minutes
    }

    private fun bedtimeStdDev(minutes: List<Double>): Double? {
        if (minutes.size < 2) return null
        val average = minutes.average()
        val variance = minutes.sumOf { (it - average) * (it - average) } / (minutes.size - 1)
        return sqrt(variance)
    }
}
