@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.fitdroid.feature.activity

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
import com.fitdroid.core.designsystem.component.ScoreRing
import com.fitdroid.core.designsystem.component.Sparkline
import com.fitdroid.core.designsystem.theme.FitdroidTheme
import com.fitdroid.core.model.ActivityScore
import com.fitdroid.core.model.ActivityScoreBreakdown
import com.fitdroid.core.model.DailyMetrics
import com.fitdroid.core.model.ExerciseSession
import com.fitdroid.core.ui.Formatters
import com.fitdroid.core.ui.metroViewModel
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import org.orbitmvi.orbit.compose.collectAsState

@Composable
fun ActivityScreen(
    modifier: Modifier = Modifier,
    viewModel: ActivityViewModel = metroViewModel(),
) {
    val state by viewModel.collectAsState()
    ActivityContent(
        state = state,
        onRefresh = viewModel::refresh,
        onPrevious = viewModel::selectPreviousDay,
        onNext = viewModel::selectNextDay,
        modifier = modifier,
    )
}

@Composable
internal fun ActivityContent(
    state: ActivityState,
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
                text = stringResource(R.string.feature_activity_screen_title),
                style = MaterialTheme.typography.titleLargeEmphasized,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                TextButton(onClick = onPrevious, enabled = state.canGoPrevious) {
                    Text(stringResource(R.string.feature_activity_screen_previous))
                }
                Text(
                    text = Formatters.localDate(state.selectedDate),
                    style = MaterialTheme.typography.titleMediumEmphasized,
                )
                TextButton(onClick = onNext, enabled = state.canGoNext) {
                    Text(stringResource(R.string.feature_activity_screen_next))
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

                !state.hasDay -> {
                    Spacer(Modifier.height(32.dp))
                    Text(
                        text = stringResource(R.string.feature_activity_screen_empty),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                else -> {
                    Spacer(Modifier.height(12.dp))
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        ScoreRing(
                            score = state.score?.score ?: 0,
                            label = stringResource(R.string.feature_activity_screen_score_label),
                        )
                    }
                    Spacer(Modifier.height(20.dp))
                    val steps = state.metrics?.steps ?: 0L
                    Text(
                        text = stringResource(R.string.feature_activity_screen_steps),
                        style = MaterialTheme.typography.titleMediumEmphasized,
                    )
                    Text(
                        text = stringResource(
                            R.string.feature_activity_screen_steps_value,
                            steps,
                            state.stepGoal,
                        ),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Spacer(Modifier.height(8.dp))
                    FitdroidWavyProgress(
                        progress = { (steps / state.stepGoal.toFloat()).coerceIn(0f, 1f) },
                    )
                    Spacer(Modifier.height(16.dp))
                    MetricRow(
                        label = stringResource(R.string.feature_activity_screen_calories),
                        value = state.metrics?.caloriesKcal?.let { kcal ->
                            stringResource(R.string.feature_activity_screen_kcal, kcal.toInt())
                        } ?: stringResource(R.string.feature_activity_screen_dash),
                    )
                    MetricRow(
                        label = stringResource(R.string.feature_activity_screen_distance),
                        value = state.metrics?.distanceMeters?.let {
                            stringResource(R.string.feature_activity_screen_km, it / 1000.0)
                        } ?: stringResource(R.string.feature_activity_screen_dash),
                    )
                    MetricRow(
                        label = stringResource(R.string.feature_activity_screen_active_minutes),
                        value = (state.metrics?.exerciseMinutes ?: 0).toString(),
                    )
                    state.score?.breakdown?.let { breakdown ->
                        Spacer(Modifier.height(24.dp))
                        Text(
                            text = stringResource(R.string.feature_activity_screen_breakdown),
                            style = MaterialTheme.typography.titleMediumEmphasized,
                        )
                        Spacer(Modifier.height(8.dp))
                        ScoreComponentRow(
                            label = stringResource(R.string.feature_activity_screen_component_steps),
                            score = breakdown.steps,
                        )
                        ScoreComponentRow(
                            label = stringResource(R.string.feature_activity_screen_component_minutes),
                            score = breakdown.activeMinutes,
                        )
                        ScoreComponentRow(
                            label = stringResource(R.string.feature_activity_screen_component_cardio),
                            score = breakdown.cardioLoad,
                        )
                    }
                    if (state.recentScores.size >= 2) {
                        Spacer(Modifier.height(24.dp))
                        Text(
                            text = stringResource(R.string.feature_activity_screen_trend),
                            style = MaterialTheme.typography.titleMediumEmphasized,
                        )
                        Spacer(Modifier.height(8.dp))
                        Sparkline(values = state.recentScores)
                    }
                    if (state.exercises.isNotEmpty()) {
                        Spacer(Modifier.height(24.dp))
                        Text(
                            text = stringResource(R.string.feature_activity_screen_workouts),
                            style = MaterialTheme.typography.titleMediumEmphasized,
                        )
                        Spacer(Modifier.height(8.dp))
                        state.exercises.forEach { session ->
                            WorkoutRow(session = session)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricRow(label: String, value: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
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

@Composable
private fun WorkoutRow(session: ExerciseSession, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            Text(
                text = session.activityType.replace('_', ' '),
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = Formatters.duration(session.duration),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        session.caloriesKcal?.let { kcal ->
            Text(
                text = stringResource(R.string.feature_activity_screen_kcal, kcal.toInt()),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ActivityContentPreview() {
    val date = LocalDate.of(2026, 9, 1)
    val zone = ZoneOffset.UTC
    FitdroidTheme(dynamicColor = false) {
        ActivityContent(
            state = ActivityState(
                isLoading = false,
                selectedDate = date,
                today = date,
                scoresByDate = mapOf(
                    date to ActivityScore(
                        date = date,
                        score = 68,
                        breakdown = ActivityScoreBreakdown(70, 60, 50),
                    ),
                ),
                metricsByDate = mapOf(
                    date to DailyMetrics(
                        date = date,
                        steps = 8_420,
                        caloriesKcal = 540.0,
                        distanceMeters = 6_200.0,
                        exerciseMinutes = 32,
                    ),
                ),
                exercisesByDate = mapOf(
                    date to listOf(
                        ExerciseSession(
                            id = "run",
                            start = date.atTime(LocalTime.of(7, 0)).toInstant(zone),
                            end = date.atTime(LocalTime.of(7, 32)).toInstant(zone),
                            activityType = "running",
                            caloriesKcal = 280.0,
                        ),
                    ),
                ),
                recentScores = listOf(50f, 62f, 58f, 68f),
            ),
            onRefresh = {},
            onPrevious = {},
            onNext = {},
        )
    }
}
