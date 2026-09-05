@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.fitdroid.feature.dashboard

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
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
import com.fitdroid.core.model.ReadinessScore
import com.fitdroid.core.model.ReadinessScoreBreakdown
import com.fitdroid.core.model.SleepScore
import com.fitdroid.core.model.SleepScoreBreakdown
import com.fitdroid.core.ui.Formatters
import com.fitdroid.core.ui.metroViewModel
import java.time.LocalDate
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun DashboardScreen(
    onOpenSleep: () -> Unit,
    onOpenActivity: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = metroViewModel(),
) {
    val state by viewModel.collectAsState()
    viewModel.collectSideEffect { effect ->
        when (effect) {
            DashboardEffect.OpenSleep -> onOpenSleep()
            DashboardEffect.OpenActivity -> onOpenActivity()
        }
    }
    DashboardContent(
        state = state,
        onRefresh = viewModel::refresh,
        onSleepClick = viewModel::onSleepClick,
        onActivityClick = viewModel::onActivityClick,
        modifier = modifier,
    )
}

@Composable
internal fun DashboardContent(
    state: DashboardState,
    onRefresh: () -> Unit,
    onSleepClick: () -> Unit,
    onActivityClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FitdroidPullToRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier.fillMaxSize(),
    ) {
        when {
            state.isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    FitdroidLoadingIndicator()
                }
            }

            !state.hasAnyScore -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = stringResource(R.string.feature_dashboard_empty_title),
                        style = MaterialTheme.typography.titleLargeEmphasized,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.feature_dashboard_empty_body),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                ) {
                    Text(
                        text = stringResource(R.string.feature_dashboard_title),
                        style = MaterialTheme.typography.titleLargeEmphasized,
                    )
                    Text(
                        text = Formatters.localDate(state.today),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(20.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        ScoreRing(
                            score = state.sleepScore?.score ?: 0,
                            label = stringResource(R.string.feature_dashboard_sleep),
                            modifier = Modifier
                                .size(96.dp)
                                .clickable(role = Role.Button, onClick = onSleepClick),
                        )
                        ScoreRing(
                            score = state.readinessScore?.score ?: 0,
                            label = stringResource(R.string.feature_dashboard_readiness),
                            modifier = Modifier.size(104.dp),
                        )
                        ScoreRing(
                            score = state.activityScore?.score ?: 0,
                            label = stringResource(R.string.feature_dashboard_activity),
                            modifier = Modifier
                                .size(96.dp)
                                .clickable(role = Role.Button, onClick = onActivityClick),
                        )
                    }
                    if (state.readinessScore?.usingDegradedModel == true) {
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = stringResource(R.string.feature_dashboard_readiness_degraded),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Spacer(Modifier.height(24.dp))
                    Text(
                        text = stringResource(R.string.feature_dashboard_sleep_trend),
                        style = MaterialTheme.typography.titleMediumEmphasized,
                    )
                    Spacer(Modifier.height(8.dp))
                    Sparkline(values = state.sleepTrend)
                    Spacer(Modifier.height(24.dp))
                    Text(
                        text = stringResource(R.string.feature_dashboard_steps),
                        style = MaterialTheme.typography.titleMediumEmphasized,
                    )
                    Spacer(Modifier.height(8.dp))
                    val steps = state.steps ?: 0L
                    Text(
                        text = stringResource(
                            R.string.feature_dashboard_steps_value,
                            steps,
                            state.stepGoal,
                        ),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Spacer(Modifier.height(8.dp))
                    FitdroidWavyProgress(
                        progress = { (steps / state.stepGoal.toFloat()).coerceIn(0f, 1f) },
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DashboardContentPreview() {
    val date = LocalDate.of(2026, 9, 1)
    FitdroidTheme(dynamicColor = false) {
        DashboardContent(
            state = DashboardState(
                isLoading = false,
                today = date,
                sleepScore = SleepScore(
                    date = date,
                    score = 84,
                    breakdown = SleepScoreBreakdown(80, 90, 85, 70, 75),
                ),
                readinessScore = ReadinessScore(
                    date = date,
                    score = 72,
                    breakdown = ReadinessScoreBreakdown(60, 80, 84, 55),
                    usingDegradedModel = false,
                ),
                activityScore = ActivityScore(
                    date = date,
                    score = 68,
                    breakdown = ActivityScoreBreakdown(70, 60, 50),
                ),
                sleepTrend = listOf(70f, 74f, 80f, 78f, 84f),
                steps = 8_420,
            ),
            onRefresh = {},
            onSleepClick = {},
            onActivityClick = {},
        )
    }
}
