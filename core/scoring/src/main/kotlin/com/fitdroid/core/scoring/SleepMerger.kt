package com.fitdroid.core.scoring

import com.fitdroid.core.model.SleepSession
import com.fitdroid.core.model.SleepStage
import com.fitdroid.core.model.SleepStageType
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

internal data class NightSleep(
    val start: Instant,
    val end: Instant,
    val stages: List<SleepStage>,
) {
    val timeInBed: Duration
        get() = Duration.between(start, end)

    val asleep: Duration
        get() = stages
            .filter {
                it.type == SleepStageType.Light ||
                    it.type == SleepStageType.Deep ||
                    it.type == SleepStageType.Rem
            }
            .fold(Duration.ZERO) { acc, stage -> acc + stage.duration }

    val restorative: Duration
        get() = stages
            .filter { it.type == SleepStageType.Deep || it.type == SleepStageType.Rem }
            .fold(Duration.ZERO) { acc, stage -> acc + stage.duration }
}

internal fun SleepSession.wakeDate(zoneId: ZoneId): LocalDate =
    end.atZone(zoneId).toLocalDate()

internal fun List<SleepSession>.nightsEndingOn(date: LocalDate, zoneId: ZoneId): NightSleep? {
    val sessions = filter { it.wakeDate(zoneId) == date }.sortedBy { it.start }
    if (sessions.isEmpty()) return null
    return NightSleep(
        start = sessions.minOf { it.start },
        end = sessions.maxOf { it.end },
        stages = sessions.flatMap { it.stages }.sortedBy { it.start },
    )
}

internal fun NightSleep.memorableAwakenings(): Int {
    val minMemorable = Duration.ofMinutes(5)
    var count = stages.count { stage ->
        (stage.type == SleepStageType.Awake || stage.type == SleepStageType.AwakeInBed) &&
            stage.duration >= minMemorable &&
            stage.start > start &&
            stage.end < end
    }
    val ordered = stages.ifEmpty {
        return count
    }
    // Gaps between non-contiguous stage blocks longer than 5 minutes also count.
    ordered.zipWithNext().forEach { (previous, next) ->
        val gap = Duration.between(previous.end, next.start)
        if (gap >= minMemorable) count++
    }
    return count
}
