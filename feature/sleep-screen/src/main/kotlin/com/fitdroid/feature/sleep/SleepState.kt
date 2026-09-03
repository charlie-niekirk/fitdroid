package com.fitdroid.feature.sleep

import com.fitdroid.core.designsystem.component.HypnogramSegment
import com.fitdroid.core.designsystem.component.TrendDirection
import com.fitdroid.core.model.SleepScore
import com.fitdroid.core.model.SleepSession
import com.fitdroid.core.model.SleepStage
import com.fitdroid.core.model.SleepStageType
import com.fitdroid.core.ui.Formatters
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale

data class SleepState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val selectedDate: LocalDate = LocalDate.EPOCH,
    val today: LocalDate = LocalDate.EPOCH,
    val sessions: List<SleepSession> = emptyList(),
    val scores: List<SleepScore> = emptyList(),
    val useClassicHypnogram: Boolean = false,
)

data class SleepUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val dateLabel: String = "",
    val canGoPrevious: Boolean = false,
    val canGoNext: Boolean = false,
    val hasNight: Boolean = false,
    val score: Int? = null,
    val bedtimeLabel: String = "",
    val wakeLabel: String = "",
    val midpointLabel: String = "",
    val timeInBedLabel: String = "",
    val asleepLabel: String = "",
    val hypnogram: List<HypnogramSegment> = emptyList(),
    val stagedHypnogram: List<HypnogramSegment> = emptyList(),
    val lightDuration: String = "",
    val deepDuration: String = "",
    val remDuration: String = "",
    val awakeDuration: String = "",
    val restlessnessDuration: String = "",
    val classicLightDuration: String = "",
    val classicDeepDuration: String = "",
    val classicRemDuration: String = "",
    val classicAwakeDuration: String = "",
    val useClassicHypnogram: Boolean = false,
    val components: List<SleepComponentUi> = emptyList(),
    val trendDelta: Int? = null,
    val trendDirection: TrendDirection = TrendDirection.Flat,
)

data class SleepComponentUi(
    val key: SleepComponent,
    val score: Int,
)

enum class SleepComponent {
    Duration,
    Restorative,
    Efficiency,
    Disturbances,
    Consistency,
}

internal const val ScoreWindowDays = 30L
internal const val TrendBaselineDays = 14L

internal fun SleepState.toUiState(
    zoneId: ZoneId,
    locale: Locale = Locale.getDefault(),
): SleepUiState {
    if (isLoading) {
        return SleepUiState(isLoading = true, isRefreshing = isRefreshing)
    }
    val nightSessions = sessions
        .filter { it.end.atZone(zoneId).toLocalDate() == selectedDate }
        .sortedBy { it.start }
    val nightScore = scores.firstOrNull { it.date == selectedDate }
    val display = displayData(nightSessions, nightScore)
    val trendDirection = when {
        display.trendDelta == null || display.trendDelta == 0 -> TrendDirection.Flat
        display.trendDelta > 0 -> TrendDirection.Up
        else -> TrendDirection.Down
    }
    return SleepUiState(
        isLoading = false,
        isRefreshing = isRefreshing,
        dateLabel = Formatters.localDate(selectedDate, locale),
        canGoPrevious = selectedDate > today.minusDays(ScoreWindowDays - 1),
        canGoNext = selectedDate < today,
        hasNight = nightSessions.isNotEmpty(),
        score = nightScore?.score,
        bedtimeLabel = display.start?.let { Formatters.timeOfDay(it, zoneId, locale) }.orEmpty(),
        wakeLabel = display.end?.let { Formatters.timeOfDay(it, zoneId, locale) }.orEmpty(),
        midpointLabel = display.start
            ?.plusMillis(display.timeInBed.toMillis() / 2)
            ?.let { Formatters.timeOfDay(it, zoneId, locale) }
            .orEmpty(),
        timeInBedLabel = Formatters.duration(display.timeInBed),
        asleepLabel = Formatters.duration(display.asleep),
        hypnogram = display.classicHypnogram,
        stagedHypnogram = display.stagedHypnogram,
        lightDuration = Formatters.duration(display.stagedStages.base.total(SleepStageType.Light)),
        deepDuration = Formatters.duration(display.stagedStages.base.total(SleepStageType.Deep)),
        remDuration = Formatters.duration(display.stagedStages.base.total(SleepStageType.Rem)),
        awakeDuration = Formatters.duration(display.stagedStages.base.total(SleepStageType.Awake)),
        restlessnessDuration = Formatters.duration(
            display.stagedStages.restlessness.total(SleepStageType.AwakeInBed),
        ),
        classicLightDuration = Formatters.duration(display.stages.total(SleepStageType.Light)),
        classicDeepDuration = Formatters.duration(display.stages.total(SleepStageType.Deep)),
        classicRemDuration = Formatters.duration(display.stages.total(SleepStageType.Rem)),
        classicAwakeDuration = Formatters.duration(
            display.stages.total(SleepStageType.Awake) +
                display.stages.total(SleepStageType.AwakeInBed),
        ),
        useClassicHypnogram = useClassicHypnogram,
        components = nightScore?.let { score ->
            listOf(
                SleepComponentUi(SleepComponent.Duration, score.breakdown.duration),
                SleepComponentUi(SleepComponent.Restorative, score.breakdown.restorative),
                SleepComponentUi(SleepComponent.Efficiency, score.breakdown.efficiency),
                SleepComponentUi(SleepComponent.Disturbances, score.breakdown.disturbances),
                SleepComponentUi(SleepComponent.Consistency, score.breakdown.consistency),
            )
        }.orEmpty(),
        trendDelta = display.trendDelta,
        trendDirection = trendDirection,
    )
}

private fun SleepState.displayData(
    nightSessions: List<SleepSession>,
    nightScore: SleepScore?,
): SleepDisplayData {
    val stages = nightSessions.flatMap { it.stages }.sortedBy { it.start }
    val start = nightSessions.minOfOrNull { it.start }
    val end = nightSessions.maxOfOrNull { it.end }
    val timeInBed = if (start != null && end != null) Duration.between(start, end) else Duration.ZERO
    val windowMillis = timeInBed.toMillis().coerceAtLeast(1L)
    val stagedStages = stages.toStagedStages(start, end)
    val asleep = stagedStages.base
        .filter { it.type in AsleepStageTypes }
        .fold(Duration.ZERO) { acc, stage -> acc + stage.duration }
    val baseline = scores.filter {
        it.date != selectedDate && it.date >= selectedDate.minusDays(TrendBaselineDays)
    }
    val average = baseline.map { it.score }.average().takeIf { baseline.isNotEmpty() }
    val trendDelta = nightScore?.score?.let { current ->
        average?.let { current - it.toInt() }
    }
    return SleepDisplayData(
        stages = stages,
        classicHypnogram = stages.map { stage ->
            stage.toHypnogramSegment(start, windowMillis, classic = true)
        },
        stagedStages = stagedStages,
        stagedHypnogram = (stagedStages.visualBase + stagedStages.restlessness)
            .sortedBy { it.start }
            .map { stage -> stage.toHypnogramSegment(start, windowMillis) },
        start = start,
        end = end,
        timeInBed = timeInBed,
        asleep = asleep,
        trendDelta = trendDelta,
    )
}

private data class SleepDisplayData(
    val stages: List<SleepStage>,
    val classicHypnogram: List<HypnogramSegment>,
    val stagedStages: StagedStages,
    val stagedHypnogram: List<HypnogramSegment>,
    val start: Instant?,
    val end: Instant?,
    val timeInBed: Duration,
    val asleep: Duration,
    val trendDelta: Int?,
)

private fun List<SleepStage>.total(type: SleepStageType): Duration =
    filter { it.type == type }.fold(Duration.ZERO) { acc, stage -> acc + stage.duration }

private val AsleepStageTypes = setOf(
    SleepStageType.Light,
    SleepStageType.Deep,
    SleepStageType.Rem,
)

private val RestlessnessMaximumDuration: Duration = Duration.ofMinutes(5)
private val VisualInterruptionMaximumDuration: Duration = Duration.ofMinutes(2)

private data class StagedStages(
    val base: List<SleepStage>,
    val visualBase: List<SleepStage>,
    val restlessness: List<SleepStage>,
)

private fun List<SleepStage>.toStagedStages(
    nightStart: Instant?,
    nightEnd: Instant?,
): StagedStages {
    val classified = sortedBy { it.start }.map { stage ->
        val isShortInteriorAwake =
            stage.type == SleepStageType.Awake &&
                stage.duration < RestlessnessMaximumDuration &&
                nightStart != null &&
                nightEnd != null &&
                stage.start > nightStart &&
                stage.end < nightEnd
        stage to (stage.type == SleepStageType.AwakeInBed || isShortInteriorAwake)
    }
    val restlessness = classified
        .filter { it.second }
        .map { (stage) -> stage.copy(type = SleepStageType.AwakeInBed) }
    val base = classified
        .map { (stage, isRestlessness) ->
            if (isRestlessness) stage.copy(type = SleepStageType.Light) else stage
        }
        .mergeAdjacentStages()
    return StagedStages(
        base = base,
        visualBase = base.smoothShortInterruptions(),
        restlessness = restlessness,
    )
}

private fun List<SleepStage>.mergeAdjacentStages(): List<SleepStage> =
    fold(mutableListOf<SleepStage>()) { merged, stage ->
        val previous = merged.lastOrNull()
        if (
            previous != null &&
            previous.type == stage.type &&
            !stage.start.isAfter(previous.end)
        ) {
            merged[merged.lastIndex] = previous.copy(end = maxOf(previous.end, stage.end))
        } else {
            merged += stage
        }
        merged
    }

private fun List<SleepStage>.smoothShortInterruptions(): List<SleepStage> {
    var result = this
    while (true) {
        val interruptionIndex = result.indices.firstOrNull { index ->
            if (index == 0 || index == result.lastIndex) return@firstOrNull false
            val previous = result[index - 1]
            val interruption = result[index]
            val next = result[index + 1]
            previous.type == next.type &&
                interruption.type != previous.type &&
                previous.type in AsleepStageTypes &&
                interruption.type in AsleepStageTypes &&
                interruption.duration <= VisualInterruptionMaximumDuration &&
                !interruption.start.isAfter(previous.end) &&
                !next.start.isAfter(interruption.end)
        } ?: return result
        result = result
            .mapIndexed { index, stage ->
                if (index == interruptionIndex) {
                    stage.copy(type = result[index - 1].type)
                } else {
                    stage
                }
            }
            .mergeAdjacentStages()
    }
}

private fun SleepStage.toHypnogramSegment(
    nightStart: Instant?,
    windowMillis: Long,
    classic: Boolean = false,
): HypnogramSegment =
    HypnogramSegment(
        type = if (classic && type == SleepStageType.AwakeInBed) SleepStageType.Awake else type,
        duration = duration,
        startFraction = nightStart?.let {
            (Duration.between(it, start).toMillis() / windowMillis.toFloat()).coerceIn(0f, 1f)
        } ?: 0f,
        endFraction = nightStart?.let {
            (Duration.between(it, end).toMillis() / windowMillis.toFloat()).coerceIn(0f, 1f)
        } ?: 0f,
    )
