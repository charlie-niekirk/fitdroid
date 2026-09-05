@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.fitdroid.feature.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fitdroid.core.designsystem.component.FitdroidLoadingIndicator
import com.fitdroid.core.designsystem.theme.FitdroidTheme
import com.fitdroid.core.health.HealthConnectLauncher
import com.fitdroid.core.model.SyncState
import com.fitdroid.core.model.UserSettings
import com.fitdroid.core.ui.Formatters
import com.fitdroid.core.ui.metroViewModel
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = metroViewModel(),
) {
    val state by viewModel.collectAsState()
    val context = LocalContext.current
    val authLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        val data = result.data
        if (data != null) {
            viewModel.onAuthorizationResult(data)
        } else {
            viewModel.onAuthorizationCancelled()
        }
    }
    viewModel.collectSideEffect { effect ->
        when (effect) {
            is SettingsEffect.LaunchGoogleHealthAuth -> authLauncher.launch(effect.intent)

            SettingsEffect.OpenHealthConnectSettings ->
                runCatching { context.startActivity(HealthConnectLauncher.settingsIntent()) }
        }
    }
    SettingsContent(
        state = state,
        onGoalDelta = viewModel::adjustGoal,
        onTogglePeriodicSync = viewModel::setPeriodicSyncEnabled,
        onToggleClassicHypnogram = viewModel::setUseClassicHypnogram,
        onSyncNow = viewModel::syncNow,
        onOpenHealthConnect = viewModel::openHealthConnect,
        onLinkGoogleHealth = viewModel::linkGoogleHealth,
        onUnlinkGoogleHealth = viewModel::unlinkGoogleHealth,
        modifier = modifier,
    )
}

@Composable
internal fun SettingsContent(
    state: SettingsState,
    onGoalDelta: (GoalField, Int) -> Unit,
    onTogglePeriodicSync: (Boolean) -> Unit,
    onToggleClassicHypnogram: (Boolean) -> Unit,
    onSyncNow: () -> Unit,
    onOpenHealthConnect: () -> Unit,
    onLinkGoogleHealth: () -> Unit,
    onUnlinkGoogleHealth: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Text(
            text = stringResource(R.string.feature_settings_title),
            style = MaterialTheme.typography.titleLargeEmphasized,
        )
        if (state.isLoading) {
            Spacer(Modifier.height(48.dp))
            FitdroidLoadingIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            SettingsLoadedContent(
                state = state,
                onGoalDelta = onGoalDelta,
                onTogglePeriodicSync = onTogglePeriodicSync,
                onToggleClassicHypnogram = onToggleClassicHypnogram,
                onSyncNow = onSyncNow,
                onOpenHealthConnect = onOpenHealthConnect,
                onLinkGoogleHealth = onLinkGoogleHealth,
                onUnlinkGoogleHealth = onUnlinkGoogleHealth,
            )
        }
    }
}

@Composable
private fun SettingsLoadedContent(
    state: SettingsState,
    onGoalDelta: (GoalField, Int) -> Unit,
    onTogglePeriodicSync: (Boolean) -> Unit,
    onToggleClassicHypnogram: (Boolean) -> Unit,
    onSyncNow: () -> Unit,
    onOpenHealthConnect: () -> Unit,
    onLinkGoogleHealth: () -> Unit,
    onUnlinkGoogleHealth: () -> Unit,
) {
    Column {
        Spacer(Modifier.height(16.dp))
        SectionTitle(stringResource(R.string.feature_settings_goals))
        GoalStepper(
            label = stringResource(R.string.feature_settings_sleep_target),
            value = Formatters.duration(Duration.ofMinutes(state.settings.sleepTargetMinutes.toLong())),
            onDecrease = { onGoalDelta(GoalField.SleepTarget, -1) },
            onIncrease = { onGoalDelta(GoalField.SleepTarget, 1) },
        )
        GoalStepper(
            label = stringResource(R.string.feature_settings_steps),
            value = state.settings.steps.toString(),
            onDecrease = { onGoalDelta(GoalField.Steps, -1) },
            onIncrease = { onGoalDelta(GoalField.Steps, 1) },
        )
        GoalStepper(
            label = stringResource(R.string.feature_settings_active_minutes),
            value = state.settings.activeMinutes.toString(),
            onDecrease = { onGoalDelta(GoalField.ActiveMinutes, -1) },
            onIncrease = { onGoalDelta(GoalField.ActiveMinutes, 1) },
        )
        GoalStepper(
            label = stringResource(R.string.feature_settings_cardio_minutes),
            value = state.settings.cardioMinutes.toString(),
            onDecrease = { onGoalDelta(GoalField.CardioMinutes, -1) },
            onIncrease = { onGoalDelta(GoalField.CardioMinutes, 1) },
        )
        Spacer(Modifier.height(24.dp))
        SectionTitle(stringResource(R.string.feature_settings_display))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = stringResource(R.string.feature_settings_classic_hypnogram),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
            )
            Switch(
                checked = state.settings.useClassicHypnogram,
                onCheckedChange = onToggleClassicHypnogram,
            )
        }
        Text(
            text = stringResource(R.string.feature_settings_classic_hypnogram_supporting),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(24.dp))
        SectionTitle(stringResource(R.string.feature_settings_sync))
        SyncStatusRow(
            label = stringResource(R.string.feature_settings_health_connect),
            syncState = state.healthConnect,
        )
        SyncStatusRow(
            label = stringResource(R.string.feature_settings_google_health),
            syncState = state.googleHealth,
        )
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = onSyncNow,
            enabled = !state.isSyncing,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.feature_settings_sync_now))
        }
        TextButton(
            onClick = onOpenHealthConnect,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.feature_settings_open_health_connect))
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = stringResource(R.string.feature_settings_periodic),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
            )
            Switch(
                checked = state.settings.periodicSyncEnabled,
                onCheckedChange = onTogglePeriodicSync,
            )
        }
        Spacer(Modifier.height(24.dp))
        SectionTitle(stringResource(R.string.feature_settings_account))
        Text(
            text = if (state.googleHealthLinked) {
                stringResource(R.string.feature_settings_google_linked)
            } else {
                stringResource(R.string.feature_settings_google_not_linked)
            },
            style = MaterialTheme.typography.bodyLarge,
        )
        if (!state.googleHealthEnabled) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.feature_settings_google_disabled),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (state.isLinkingGoogleHealth) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.feature_settings_linking),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        state.googleHealthError?.let { error ->
            Spacer(Modifier.height(8.dp))
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
            )
        }
        Spacer(Modifier.height(8.dp))
        if (state.googleHealthLinked) {
            OutlinedButton(
                onClick = onUnlinkGoogleHealth,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.feature_settings_unlink))
            }
        } else if (state.googleHealthEnabled) {
            Button(
                onClick = onLinkGoogleHealth,
                enabled = !state.isLinkingGoogleHealth,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.feature_settings_link))
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMediumEmphasized,
        modifier = modifier.padding(bottom = 8.dp),
    )
}

@Composable
private fun GoalStepper(
    label: String,
    value: String,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = onDecrease) {
            Icon(
                imageVector = Icons.Filled.Remove,
                contentDescription = stringResource(R.string.feature_settings_decrease),
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.titleMediumEmphasized,
        )
        IconButton(onClick = onIncrease) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = stringResource(R.string.feature_settings_increase),
            )
        }
    }
}

@Composable
private fun SyncStatusRow(
    label: String,
    syncState: SyncState?,
    modifier: Modifier = Modifier,
) {
    val zone = ZoneOffset.systemDefault()
    val successAt = syncState?.lastSuccessAt
    val attemptAt = syncState?.lastAttemptAt
    val error = syncState?.lastError
    val body = when {
        error != null && successAt == null -> error

        successAt != null -> stringResource(R.string.feature_settings_last_sync, Formatters.dateTime(successAt, zone))

        attemptAt != null -> stringResource(
            R.string.feature_settings_last_attempt,
            Formatters.dateTime(attemptAt, zone),
        )

        else -> stringResource(R.string.feature_settings_never)
    }
    Column(modifier = modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsContentPreview() {
    FitdroidTheme(dynamicColor = false) {
        SettingsContent(
            state = SettingsState(
                isLoading = false,
                settings = UserSettings.Default,
                healthConnect = SyncState(
                    source = SyncState.SOURCE_HEALTH_CONNECT,
                    lastSuccessAt = Instant.parse("2026-09-02T10:14:00Z"),
                    lastAttemptAt = Instant.parse("2026-09-02T10:14:00Z"),
                ),
                googleHealthEnabled = false,
                googleHealthLinked = false,
            ),
            onGoalDelta = { _, _ -> },
            onTogglePeriodicSync = {},
            onToggleClassicHypnogram = {},
            onSyncNow = {},
            onOpenHealthConnect = {},
            onLinkGoogleHealth = {},
            onUnlinkGoogleHealth = {},
        )
    }
}
