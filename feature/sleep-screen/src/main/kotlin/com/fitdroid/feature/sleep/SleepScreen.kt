@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.fitdroid.feature.sleep

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
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
import com.fitdroid.core.designsystem.component.FitdroidPullToRefreshBox
import com.fitdroid.core.designsystem.component.FitdroidWavyProgress
import com.fitdroid.core.designsystem.component.Hypnogram
import com.fitdroid.core.designsystem.component.HypnogramSegment
import com.fitdroid.core.designsystem.component.ScoreRing
import com.fitdroid.core.designsystem.component.StagedHypnogram
import com.fitdroid.core.designsystem.component.TrendChip
import com.fitdroid.core.designsystem.component.TrendDirection
import com.fitdroid.core.designsystem.theme.FitdroidTheme
import com.fitdroid.core.model.SleepStageType
import com.fitdroid.core.ui.metroViewModel
import java.time.Duration
import org.orbitmvi.orbit.compose.collectAsState

@Composable
fun SleepScreen(
    modifier: Modifier = Modifier,
    viewModel: SleepViewModel = metroViewModel(),
) {
    val state by viewModel.collectAsState()
    SleepContent(
        state = state,
        onRefresh = viewModel::refresh,
        onPrevious = viewModel::selectPreviousNight,
        onNext = viewModel::selectNextNight,
        modifier = modifier,
    )
}

@Composable
internal fun SleepContent(
    state: SleepUiState,
    onRefresh: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FitdroidPullToRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            Text(
                text = stringResource(R.string.feature_sleep_screen_title),
                style = MaterialTheme.typography.titleLargeEmphasized,
            )
            DayPager(
                dateLabel = state.dateLabel,
                canGoPrevious = state.canGoPrevious,
                canGoNext = state.canGoNext,
                onPrevious = onPrevious,
                onNext = onNext,
            )
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

                !state.hasNight -> {
                    Spacer(Modifier.height(32.dp))
                    Text(
                        text = stringResource(R.string.feature_sleep_screen_empty),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                else -> {
                    Spacer(Modifier.height(12.dp))
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        ScoreRing(
                            score = state.score ?: 0,
                            label = stringResource(R.string.feature_sleep_screen_score_label),
                        )
                    }
                    if (state.trendDelta != null) {
                        Spacer(Modifier.height(8.dp))
                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            TrendChip(
                                text = stringResource(
                                    R.string.feature_sleep_screen_trend,
                                    state.trendDelta,
                                ),
                                direction = state.trendDirection,
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = stringResource(
                            R.string.feature_sleep_screen_window,
                            state.bedtimeLabel,
                            state.wakeLabel,
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text = stringResource(
                            R.string.feature_sleep_screen_asleep_in_bed,
                            state.asleepLabel,
                            state.timeInBedLabel,
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(16.dp))
                    if (state.useClassicHypnogram) {
                        Hypnogram(segments = state.hypnogram)
                        Spacer(Modifier.height(16.dp))
                        StageRow(
                            stringResource(R.string.feature_sleep_screen_deep),
                            state.classicDeepDuration,
                        )
                        StageRow(
                            stringResource(R.string.feature_sleep_screen_rem),
                            state.classicRemDuration,
                        )
                        StageRow(
                            stringResource(R.string.feature_sleep_screen_light),
                            state.classicLightDuration,
                        )
                        StageRow(
                            stringResource(R.string.feature_sleep_screen_awake),
                            state.classicAwakeDuration,
                        )
                    } else {
                        StagedHypnogram(
                            segments = state.stagedHypnogram,
                            awakeLabel = stringResource(R.string.feature_sleep_screen_awake),
                            awakeDuration = state.awakeDuration,
                            restlessnessLabel = stringResource(
                                R.string.feature_sleep_screen_restlessness,
                            ),
                            restlessnessDuration = state.restlessnessDuration,
                            remLabel = stringResource(R.string.feature_sleep_screen_rem),
                            remDuration = state.remDuration,
                            lightLabel = stringResource(R.string.feature_sleep_screen_light),
                            lightDuration = state.lightDuration,
                            deepLabel = stringResource(R.string.feature_sleep_screen_deep),
                            deepDuration = state.deepDuration,
                            startTimeLabel = state.bedtimeLabel,
                            midpointTimeLabel = state.midpointLabel,
                            endTimeLabel = state.wakeLabel,
                        )
                    }
                    Spacer(Modifier.height(24.dp))
                    Text(
                        text = stringResource(R.string.feature_sleep_screen_breakdown),
                        style = MaterialTheme.typography.titleMediumEmphasized,
                    )
                    Spacer(Modifier.height(8.dp))
                    state.components.forEach { component ->
                        ScoreComponentRow(
                            label = stringResource(component.key.labelRes()),
                            score = component.score,
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun DayPager(
    dateLabel: String,
    canGoPrevious: Boolean,
    canGoNext: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        TextButton(onClick = onPrevious, enabled = canGoPrevious) {
            Text(stringResource(R.string.feature_sleep_screen_previous))
        }
        Text(text = dateLabel, style = MaterialTheme.typography.titleMediumEmphasized)
        TextButton(onClick = onNext, enabled = canGoNext) {
            Text(stringResource(R.string.feature_sleep_screen_next))
        }
    }
}

@Composable
private fun StageRow(label: String, duration: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Text(text = duration, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun ScoreComponentRow(label: String, score: Int, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(text = label, style = MaterialTheme.typography.bodyMedium)
            Text(text = score.toString(), style = MaterialTheme.typography.bodyMedium)
        }
        Spacer(Modifier.height(4.dp))
        FitdroidWavyProgress(
            progress = { (score / 100f).coerceIn(0f, 1f) },
        )
    }
}

private fun SleepComponent.labelRes(): Int = when (this) {
    SleepComponent.Duration -> R.string.feature_sleep_screen_component_duration
    SleepComponent.Restorative -> R.string.feature_sleep_screen_component_restorative
    SleepComponent.Efficiency -> R.string.feature_sleep_screen_component_efficiency
    SleepComponent.Disturbances -> R.string.feature_sleep_screen_component_disturbances
    SleepComponent.Consistency -> R.string.feature_sleep_screen_component_consistency
}

@Preview(showBackground = true)
@Composable
private fun SleepContentPreview() {
    FitdroidTheme(dynamicColor = false) {
        SleepContent(
            state = SleepUiState(
                isLoading = false,
                dateLabel = "Sep 1, 2026",
                canGoPrevious = true,
                canGoNext = false,
                hasNight = true,
                score = 84,
                bedtimeLabel = "10:12 PM",
                wakeLabel = "6:04 AM",
                midpointLabel = "2:08 AM",
                timeInBedLabel = "7h 52m",
                asleepLabel = "7h 21m",
                hypnogram = listOf(
                    HypnogramSegment(SleepStageType.Awake, Duration.ofMinutes(12), 0f, 0.025f),
                    HypnogramSegment(SleepStageType.Light, Duration.ofMinutes(90), 0.025f, 0.31f),
                    HypnogramSegment(SleepStageType.Deep, Duration.ofMinutes(70), 0.31f, 0.53f),
                    HypnogramSegment(SleepStageType.Rem, Duration.ofMinutes(45), 0.53f, 0.67f),
                    HypnogramSegment(SleepStageType.Light, Duration.ofMinutes(100), 0.67f, 1f),
                ),
                stagedHypnogram = listOf(
                    HypnogramSegment(SleepStageType.Awake, Duration.ofMinutes(12), 0f, 0.025f),
                    HypnogramSegment(SleepStageType.Light, Duration.ofMinutes(90), 0.025f, 0.31f),
                    HypnogramSegment(
                        SleepStageType.AwakeInBed,
                        Duration.ofMinutes(2),
                        0.12f,
                        0.124f,
                    ),
                    HypnogramSegment(SleepStageType.Deep, Duration.ofMinutes(70), 0.31f, 0.53f),
                    HypnogramSegment(SleepStageType.Rem, Duration.ofMinutes(45), 0.53f, 0.67f),
                    HypnogramSegment(SleepStageType.Light, Duration.ofMinutes(100), 0.67f, 1f),
                ),
                lightDuration = "3h 10m",
                deepDuration = "1h 40m",
                remDuration = "1h 25m",
                awakeDuration = "18m",
                restlessnessDuration = "19m",
                classicLightDuration = "3h 10m",
                classicDeepDuration = "1h 40m",
                classicRemDuration = "1h 25m",
                classicAwakeDuration = "18m",
                components = listOf(
                    SleepComponentUi(SleepComponent.Duration, 82),
                    SleepComponentUi(SleepComponent.Restorative, 90),
                    SleepComponentUi(SleepComponent.Efficiency, 86),
                    SleepComponentUi(SleepComponent.Disturbances, 70),
                    SleepComponentUi(SleepComponent.Consistency, 75),
                ),
                trendDelta = 4,
                trendDirection = TrendDirection.Up,
            ),
            onRefresh = {},
            onPrevious = {},
            onNext = {},
        )
    }
}
