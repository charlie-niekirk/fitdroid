@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.fitdroid.feature.reports

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fitdroid.core.designsystem.component.FitdroidLoadingIndicator
import com.fitdroid.core.designsystem.component.FitdroidWavyProgress
import com.fitdroid.core.designsystem.component.ScoreRing
import com.fitdroid.core.designsystem.component.Sparkline
import com.fitdroid.core.designsystem.component.TrendChip
import com.fitdroid.core.designsystem.component.TrendDirection
import com.fitdroid.core.designsystem.theme.FitdroidTheme
import com.fitdroid.core.ui.metroViewModel
import org.orbitmvi.orbit.compose.collectAsState

@Composable
fun ReportsScreen(
    modifier: Modifier = Modifier,
    viewModel: ReportsViewModel = metroViewModel(),
) {
    val state by viewModel.collectAsState()
    ReportsContent(
        state = state,
        onSelectPeriod = viewModel::selectPeriod,
        onPrevious = viewModel::selectPreviousPeriod,
        onNext = viewModel::selectNextPeriod,
        modifier = modifier,
    )
}

@Composable
internal fun ReportsContent(
    state: ReportsUiState,
    onSelectPeriod: (ReportPeriod) -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Text(
            text = stringResource(R.string.feature_reports_title),
            style = MaterialTheme.typography.titleLargeEmphasized,
        )
        Spacer(Modifier.height(12.dp))
        PeriodSelector(period = state.period, onSelectPeriod = onSelectPeriod)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            TextButton(onClick = onPrevious, enabled = state.canGoPrevious) {
                Text(stringResource(R.string.feature_reports_previous))
            }
            Text(text = state.periodLabel, style = MaterialTheme.typography.titleMediumEmphasized)
            TextButton(onClick = onNext, enabled = state.canGoNext) {
                Text(stringResource(R.string.feature_reports_next))
            }
        }
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    FitdroidLoadingIndicator()
                }
            }

            !state.hasData -> {
                Spacer(Modifier.height(32.dp))
                Text(
                    text = stringResource(R.string.feature_reports_empty),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            else -> {
                Text(
                    text = stringResource(
                        R.string.feature_reports_days,
                        state.daysWithScores,
                        state.daysInPeriod,
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ScoreRing(
                        score = state.sleepAverage ?: 0,
                        label = stringResource(R.string.feature_reports_sleep),
                        modifier = Modifier.size(96.dp),
                    )
                    ScoreRing(
                        score = state.readinessAverage ?: 0,
                        label = stringResource(R.string.feature_reports_readiness),
                        modifier = Modifier.size(104.dp),
                    )
                    ScoreRing(
                        score = state.activityAverage ?: 0,
                        label = stringResource(R.string.feature_reports_activity),
                        modifier = Modifier.size(96.dp),
                    )
                }
                AverageTrendRow(
                    sleepDelta = state.sleepDelta,
                    sleepDirection = state.sleepTrendDirection,
                    readinessDelta = state.readinessDelta,
                    readinessDirection = state.readinessTrendDirection,
                    activityDelta = state.activityDelta,
                    activityDirection = state.activityTrendDirection,
                )
                if (state.readinessDegraded) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.feature_reports_readiness_degraded),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                TrendSection(
                    title = stringResource(R.string.feature_reports_sleep_trend),
                    values = state.sleepTrend,
                )
                TrendSection(
                    title = stringResource(R.string.feature_reports_readiness_trend),
                    values = state.readinessTrend,
                )
                TrendSection(
                    title = stringResource(R.string.feature_reports_activity_trend),
                    values = state.activityTrend,
                )
                ComponentSection(
                    title = stringResource(R.string.feature_reports_sleep_breakdown),
                    components = state.sleepComponents,
                )
                ComponentSection(
                    title = stringResource(R.string.feature_reports_readiness_breakdown),
                    components = state.readinessComponents,
                )
                ComponentSection(
                    title = stringResource(R.string.feature_reports_activity_breakdown),
                    components = state.activityComponents,
                )
            }
        }
    }
}

@Composable
private fun PeriodSelector(
    period: ReportPeriod,
    onSelectPeriod: (ReportPeriod) -> Unit,
    modifier: Modifier = Modifier,
) {
    val periods = ReportPeriod.entries
    SingleChoiceSegmentedButtonRow(modifier = modifier.fillMaxWidth()) {
        periods.forEachIndexed { index, value ->
            SegmentedButton(
                selected = period == value,
                onClick = { onSelectPeriod(value) },
                shape = SegmentedButtonDefaults.itemShape(index, periods.size),
                label = {
                    Text(
                        text = stringResource(
                            when (value) {
                                ReportPeriod.Week -> R.string.feature_reports_week
                                ReportPeriod.Month -> R.string.feature_reports_month
                            },
                        ),
                    )
                },
            )
        }
    }
}

@Composable
private fun AverageTrendRow(
    sleepDelta: Int?,
    sleepDirection: TrendDirection,
    readinessDelta: Int?,
    readinessDirection: TrendDirection,
    activityDelta: Int?,
    activityDirection: TrendDirection,
    modifier: Modifier = Modifier,
) {
    val chips = listOf(
        sleepDelta to sleepDirection,
        readinessDelta to readinessDirection,
        activityDelta to activityDirection,
    ).mapNotNull { (delta, direction) ->
        delta?.let { it to direction }
    }
    if (chips.isEmpty()) return
    Column(modifier = modifier) {
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            chips.forEach { (delta, direction) ->
                TrendChip(
                    text = stringResource(R.string.feature_reports_trend, delta),
                    direction = direction,
                )
            }
        }
    }
}

@Composable
private fun TrendSection(title: String, values: List<Float>, modifier: Modifier = Modifier) {
    if (values.size < 2) return
    Column(modifier = modifier) {
        Spacer(Modifier.height(24.dp))
        Text(text = title, style = MaterialTheme.typography.titleMediumEmphasized)
        Spacer(Modifier.height(8.dp))
        Sparkline(values = values)
    }
}

@Composable
private fun ComponentSection(
    title: String,
    components: List<ReportComponentUi>,
    modifier: Modifier = Modifier,
) {
    if (components.isEmpty()) return
    Column(modifier = modifier) {
        Spacer(Modifier.height(24.dp))
        Text(text = title, style = MaterialTheme.typography.titleMediumEmphasized)
        Spacer(Modifier.height(8.dp))
        components.forEach { component ->
            ScoreComponentRow(
                label = stringResource(component.key.labelRes()),
                score = component.score,
            )
        }
    }
}

@Composable
private fun ScoreComponentRow(label: String, score: Int?, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(text = label, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = score?.toString() ?: stringResource(R.string.feature_reports_dash),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        Spacer(Modifier.height(4.dp))
        FitdroidWavyProgress(
            progress = { ((score ?: 0) / 100f).coerceIn(0f, 1f) },
        )
    }
}

private fun ReportComponent.labelRes(): Int = when (this) {
    ReportComponent.Duration -> R.string.feature_reports_component_duration
    ReportComponent.Restorative -> R.string.feature_reports_component_restorative
    ReportComponent.Efficiency -> R.string.feature_reports_component_efficiency
    ReportComponent.Disturbances -> R.string.feature_reports_component_disturbances
    ReportComponent.Consistency -> R.string.feature_reports_component_consistency
    ReportComponent.Hrv -> R.string.feature_reports_component_hrv
    ReportComponent.RestingHeartRate -> R.string.feature_reports_component_rhr
    ReportComponent.Sleep -> R.string.feature_reports_component_sleep
    ReportComponent.TrainingLoad -> R.string.feature_reports_component_training
    ReportComponent.Steps -> R.string.feature_reports_component_steps
    ReportComponent.ActiveMinutes -> R.string.feature_reports_component_minutes
    ReportComponent.CardioLoad -> R.string.feature_reports_component_cardio
}

@Preview(showBackground = true)
@Composable
private fun ReportsContentPreview() {
    FitdroidTheme(dynamicColor = false) {
        ReportsContent(
            state = ReportsUiState(
                isLoading = false,
                period = ReportPeriod.Week,
                periodLabel = "Aug 31 – Sep 6",
                canGoPrevious = true,
                canGoNext = false,
                hasData = true,
                daysWithScores = 5,
                daysInPeriod = 7,
                sleepAverage = 82,
                readinessAverage = 74,
                activityAverage = 68,
                sleepTrend = listOf(78f, 80f, 84f, 82f, 86f),
                readinessTrend = listOf(70f, 72f, 76f, 74f, 75f),
                activityTrend = listOf(60f, 64f, 70f, 68f, 72f),
                sleepDelta = 4,
                readinessDelta = -2,
                activityDelta = 6,
                sleepTrendDirection = TrendDirection.Up,
                readinessTrendDirection = TrendDirection.Down,
                activityTrendDirection = TrendDirection.Up,
                sleepComponents = listOf(
                    ReportComponentUi(ReportComponent.Duration, 80),
                    ReportComponentUi(ReportComponent.Restorative, 88),
                    ReportComponentUi(ReportComponent.Efficiency, 84),
                    ReportComponentUi(ReportComponent.Disturbances, 72),
                    ReportComponentUi(ReportComponent.Consistency, 76),
                ),
                readinessComponents = listOf(
                    ReportComponentUi(ReportComponent.Hrv, null),
                    ReportComponentUi(ReportComponent.RestingHeartRate, 78),
                    ReportComponentUi(ReportComponent.Sleep, 82),
                    ReportComponentUi(ReportComponent.TrainingLoad, 60),
                ),
                activityComponents = listOf(
                    ReportComponentUi(ReportComponent.Steps, 70),
                    ReportComponentUi(ReportComponent.ActiveMinutes, 64),
                    ReportComponentUi(ReportComponent.CardioLoad, 55),
                ),
                readinessDegraded = true,
            ),
            onSelectPeriod = {},
            onPrevious = {},
            onNext = {},
        )
    }
}
