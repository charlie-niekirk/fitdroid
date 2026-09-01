package com.fitdroid.feature.sleep

import com.fitdroid.core.designsystem.component.HypnogramSegment
import com.fitdroid.core.designsystem.component.TrendDirection
import com.fitdroid.core.model.SleepScore
import com.fitdroid.core.model.SleepSession
import com.fitdroid.core.model.SleepStage
import com.fitdroid.core.model.SleepStageType
import com.fitdroid.core.ui.Formatters
import java.time.Duration
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
    val timeInBedLabel: String = "",
    val asleepLabel: String = "",
    val hypnogram: List<HypnogramSegment> = emptyList(),
    val lightDuration: String = "",
    val deepDuration: String = "",
    val remDuration: String = "",
    val awakeDuration: String = "",
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
    val stages = nightSessions.flatMap { it.stages }.sortedBy { it.start }
    val start = nightSessions.minOfOrNull { it.start }
    val end = nightSessions.maxOfOrNull { it.end }
    val timeInBed = if (start != null && end != null) Duration.between(start, end) else Duration.ZERO
    val asleep = nightSessions.fold(Duration.ZERO) { acc, session -> acc + session.asleepDuration }
    val baseline = scores.filter {
        it.date != selectedDate && it.date >= selectedDate.minusDays(TrendBaselineDays)
    }
    val average = baseline.map { it.score }.average().takeIf { baseline.isNotEmpty() }
    val trendDelta = nightScore?.score?.let { current ->
        average?.let { current - it.toInt() }
    }
    return SleepUiState(
        isLoading = false,
        isRefreshing = isRefreshing,
        dateLabel = Formatters.localDate(selectedDate, locale),
        canGoPrevious = selectedDate > today.minusDays(ScoreWindowDays - 1),
        canGoNext = selectedDate < today,
        hasNight = nightSessions.isNotEmpty(),
        score = nightScore?.score,
        bedtimeLabel = start?.let { Formatters.timeOfDay(it, zoneId, locale) }.orEmpty(),
        wakeLabel = end?.let { Formatters.timeOfDay(it, zoneId, locale) }.orEmpty(),
        timeInBedLabel = Formatters.duration(timeInBed),
        asleepLabel = Formatters.duration(asleep),
        hypnogram = stages.map { HypnogramSegment(it.type, it.duration) },
        lightDuration = Formatters.duration(stages.total(SleepStageType.Light)),
        deepDuration = Formatters.duration(stages.total(SleepStageType.Deep)),
        remDuration = Formatters.duration(stages.total(SleepStageType.Rem)),
        awakeDuration = Formatters.duration(stages.total(SleepStageType.Awake)),
        components = nightScore?.let { score ->
            listOf(
                SleepComponentUi(SleepComponent.Duration, score.breakdown.duration),
                SleepComponentUi(SleepComponent.Restorative, score.breakdown.restorative),
                SleepComponentUi(SleepComponent.Efficiency, score.breakdown.efficiency),
                SleepComponentUi(SleepComponent.Disturbances, score.breakdown.disturbances),
                SleepComponentUi(SleepComponent.Consistency, score.breakdown.consistency),
            )
        }.orEmpty(),
        trendDelta = trendDelta,
        trendDirection = when {
            trendDelta == null || trendDelta == 0 -> TrendDirection.Flat
            trendDelta > 0 -> TrendDirection.Up
            else -> TrendDirection.Down
        },
    )
}

private fun List<SleepStage>.total(type: SleepStageType): Duration =
    filter { it.type == type }.fold(Duration.ZERO) { acc, stage -> acc + stage.duration }
