package com.fitdroid.feature.reports

import com.fitdroid.core.designsystem.component.TrendDirection
import com.fitdroid.core.model.DailyScores
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale

enum class ReportPeriod {
    Week,
    Month,
}

data class ReportsState(
    val isLoading: Boolean = true,
    val period: ReportPeriod = ReportPeriod.Week,
    val offset: Int = 0,
    val today: LocalDate = LocalDate.EPOCH,
    val scores: List<DailyScores> = emptyList(),
)

data class ReportsUiState(
    val isLoading: Boolean = true,
    val period: ReportPeriod = ReportPeriod.Week,
    val periodLabel: String = "",
    val canGoPrevious: Boolean = false,
    val canGoNext: Boolean = false,
    val hasData: Boolean = false,
    val daysWithScores: Int = 0,
    val daysInPeriod: Int = 0,
    val sleepAverage: Int? = null,
    val readinessAverage: Int? = null,
    val activityAverage: Int? = null,
    val sleepTrend: List<Float> = emptyList(),
    val readinessTrend: List<Float> = emptyList(),
    val activityTrend: List<Float> = emptyList(),
    val sleepDelta: Int? = null,
    val readinessDelta: Int? = null,
    val activityDelta: Int? = null,
    val sleepTrendDirection: TrendDirection = TrendDirection.Flat,
    val readinessTrendDirection: TrendDirection = TrendDirection.Flat,
    val activityTrendDirection: TrendDirection = TrendDirection.Flat,
    val sleepComponents: List<ReportComponentUi> = emptyList(),
    val readinessComponents: List<ReportComponentUi> = emptyList(),
    val activityComponents: List<ReportComponentUi> = emptyList(),
    val readinessDegraded: Boolean = false,
)

data class ReportComponentUi(
    val key: ReportComponent,
    val score: Int?,
)

enum class ReportComponent {
    Duration,
    Restorative,
    Efficiency,
    Disturbances,
    Consistency,
    Hrv,
    RestingHeartRate,
    Sleep,
    TrainingLoad,
    Steps,
    ActiveMinutes,
    CardioLoad,
}

internal const val MaxWeekOffset = 52
internal const val MaxMonthOffset = 24

internal data class DateRange(
    val start: LocalDate,
    val endExclusive: LocalDate,
)

internal fun reportRange(today: LocalDate, period: ReportPeriod, offset: Int): DateRange {
    return when (period) {
        ReportPeriod.Week -> {
            val currentStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            val start = currentStart.minusWeeks(offset.toLong())
            DateRange(start, start.plusWeeks(1))
        }

        ReportPeriod.Month -> {
            val currentStart = today.withDayOfMonth(1)
            val start = currentStart.minusMonths(offset.toLong())
            DateRange(start, start.plusMonths(1))
        }
    }
}

internal fun ReportsState.toUiState(locale: Locale = Locale.getDefault()): ReportsUiState {
    if (isLoading) {
        return ReportsUiState(isLoading = true, period = period)
    }
    val range = reportRange(today, period, offset)
    val previous = reportRange(today, period, offset + 1)
    val currentDays = daysIn(range, today)
    val currentScores = scores.inRange(range)
    val previousScores = scores.inRange(previous)
    val sleepAvg = currentScores.mapNotNull { it.sleep?.score }.averageOrNull()
    val readinessAvg = currentScores.mapNotNull { it.readiness?.score }.averageOrNull()
    val activityAvg = currentScores.mapNotNull { it.activity?.score }.averageOrNull()
    val sleepDelta = delta(sleepAvg, previousScores.mapNotNull { it.sleep?.score }.averageOrNull())
    val readinessDelta = delta(readinessAvg, previousScores.mapNotNull { it.readiness?.score }.averageOrNull())
    val activityDelta = delta(activityAvg, previousScores.mapNotNull { it.activity?.score }.averageOrNull())
    return ReportsUiState(
        isLoading = false,
        period = period,
        periodLabel = range.label(locale),
        canGoPrevious = offset < maxOffset(period),
        canGoNext = offset > 0,
        hasData = currentScores.any { it.sleep != null || it.readiness != null || it.activity != null },
        daysWithScores = currentScores.count { it.sleep != null || it.readiness != null || it.activity != null },
        daysInPeriod = currentDays.size,
        sleepAverage = sleepAvg,
        readinessAverage = readinessAvg,
        activityAverage = activityAvg,
        sleepTrend = currentScores.sortedBy { it.date }.mapNotNull { it.sleep?.score?.toFloat() },
        readinessTrend = currentScores.sortedBy { it.date }.mapNotNull { it.readiness?.score?.toFloat() },
        activityTrend = currentScores.sortedBy { it.date }.mapNotNull { it.activity?.score?.toFloat() },
        sleepDelta = sleepDelta,
        readinessDelta = readinessDelta,
        activityDelta = activityDelta,
        sleepTrendDirection = sleepDelta.direction(),
        readinessTrendDirection = readinessDelta.direction(),
        activityTrendDirection = activityDelta.direction(),
        sleepComponents = currentScores.sleepComponents(),
        readinessComponents = currentScores.readinessComponents(),
        activityComponents = currentScores.activityComponents(),
        readinessDegraded = currentScores.any { it.readiness?.usingDegradedModel == true },
    )
}

internal fun maxOffset(period: ReportPeriod): Int = when (period) {
    ReportPeriod.Week -> MaxWeekOffset
    ReportPeriod.Month -> MaxMonthOffset
}

private fun daysIn(range: DateRange, today: LocalDate): List<LocalDate> =
    generateSequence(range.start) { it.plusDays(1) }
        .takeWhile { it < range.endExclusive && !it.isAfter(today) }
        .toList()

private fun List<DailyScores>.inRange(range: DateRange): List<DailyScores> =
    filter { it.date >= range.start && it.date < range.endExclusive }

private fun DateRange.label(locale: Locale): String {
    val end = endExclusive.minusDays(1)
    val monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", locale)
    val dayFormatter = DateTimeFormatter.ofPattern("MMM d", locale)
    return if (start.dayOfMonth == 1 && end == start.with(TemporalAdjusters.lastDayOfMonth())) {
        start.format(monthFormatter)
    } else {
        "${start.format(dayFormatter)} – ${end.format(dayFormatter)}"
    }
}

private fun List<Int>.averageOrNull(): Int? = takeIf { isNotEmpty() }?.average()?.toInt()

private fun delta(current: Int?, previous: Int?): Int? =
    if (current != null && previous != null) current - previous else null

private fun Int?.direction(): TrendDirection = when {
    this == null || this == 0 -> TrendDirection.Flat
    this > 0 -> TrendDirection.Up
    else -> TrendDirection.Down
}

private fun List<DailyScores>.sleepComponents(): List<ReportComponentUi> {
    val nights = mapNotNull { it.sleep }
    if (nights.isEmpty()) return emptyList()
    return listOf(
        ReportComponentUi(ReportComponent.Duration, nights.map { it.breakdown.duration }.averageOrNull()),
        ReportComponentUi(ReportComponent.Restorative, nights.map { it.breakdown.restorative }.averageOrNull()),
        ReportComponentUi(ReportComponent.Efficiency, nights.map { it.breakdown.efficiency }.averageOrNull()),
        ReportComponentUi(ReportComponent.Disturbances, nights.map { it.breakdown.disturbances }.averageOrNull()),
        ReportComponentUi(ReportComponent.Consistency, nights.map { it.breakdown.consistency }.averageOrNull()),
    )
}

private fun List<DailyScores>.readinessComponents(): List<ReportComponentUi> {
    val days = mapNotNull { it.readiness }
    if (days.isEmpty()) return emptyList()
    return listOf(
        ReportComponentUi(ReportComponent.Hrv, days.mapNotNull { it.breakdown.hrv }.averageOrNull()),
        ReportComponentUi(ReportComponent.RestingHeartRate, days.map { it.breakdown.restingHeartRate }.averageOrNull()),
        ReportComponentUi(ReportComponent.Sleep, days.map { it.breakdown.sleep }.averageOrNull()),
        ReportComponentUi(ReportComponent.TrainingLoad, days.mapNotNull { it.breakdown.trainingLoad }.averageOrNull()),
    )
}

private fun List<DailyScores>.activityComponents(): List<ReportComponentUi> {
    val days = mapNotNull { it.activity }
    if (days.isEmpty()) return emptyList()
    return listOf(
        ReportComponentUi(ReportComponent.Steps, days.map { it.breakdown.steps }.averageOrNull()),
        ReportComponentUi(ReportComponent.ActiveMinutes, days.map { it.breakdown.activeMinutes }.averageOrNull()),
        ReportComponentUi(ReportComponent.CardioLoad, days.map { it.breakdown.cardioLoad }.averageOrNull()),
    )
}
