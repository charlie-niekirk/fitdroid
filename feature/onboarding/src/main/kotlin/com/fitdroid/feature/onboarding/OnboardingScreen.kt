package com.fitdroid.feature.onboarding

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.fitdroid.core.designsystem.theme.FitdroidTheme
import com.fitdroid.core.health.HealthConnectAvailability
import com.fitdroid.core.health.HealthConnectLauncher
import com.fitdroid.core.ui.metroViewModel
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun OnboardingScreen(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel = metroViewModel(),
) {
    val state by viewModel.collectAsState()
    val context = LocalContext.current
    val permissionContract = remember { HealthConnectLauncher.permissionContract() }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = permissionContract,
    ) { granted ->
        viewModel.onPermissionsResult(granted)
    }
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

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.refresh()
    }

    viewModel.collectSideEffect { effect ->
        when (effect) {
            is OnboardingEffect.RequestHealthConnectPermissions ->
                permissionLauncher.launch(effect.permissions)
            is OnboardingEffect.LaunchGoogleHealthAuth ->
                authLauncher.launch(effect.intent)
            OnboardingEffect.OpenHealthConnectSettings ->
                runCatching { context.startActivity(HealthConnectLauncher.settingsIntent()) }
            OnboardingEffect.OpenProviderUpdate ->
                runCatching { context.startActivity(HealthConnectLauncher.providerUpdateIntent(context)) }
            OnboardingEffect.Completed -> onFinished()
        }
    }

    OnboardingContent(
        state = state,
        onRequestPermissions = viewModel::requestPermissions,
        onOpenHealthConnect = viewModel::openHealthConnect,
        onLinkGoogleHealth = viewModel::linkGoogleHealth,
        onSkipGoogleHealth = viewModel::skipGoogleHealth,
        onContinue = viewModel::continueToApp,
        onShowPrivacyPolicy = viewModel::showPrivacyPolicy,
        onHidePrivacyPolicy = viewModel::hidePrivacyPolicy,
        modifier = modifier,
    )
}

@Composable
internal fun OnboardingContent(
    state: OnboardingState,
    onRequestPermissions: () -> Unit,
    onOpenHealthConnect: () -> Unit,
    onLinkGoogleHealth: () -> Unit,
    onSkipGoogleHealth: () -> Unit,
    onContinue: () -> Unit,
    onShowPrivacyPolicy: () -> Unit,
    onHidePrivacyPolicy: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (state.step) {
        OnboardingStep.Loading -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        OnboardingStep.PrivacyPolicy -> {
            PrivacyPolicyScreen(onBack = onHidePrivacyPolicy, modifier = modifier)
        }
        OnboardingStep.Unavailable -> {
            OnboardingPane(
                title = stringResource(R.string.feature_onboarding_unavailable_title),
                body = stringResource(R.string.feature_onboarding_unavailable_body),
                modifier = modifier,
            )
        }
        OnboardingStep.UpdateRequired -> {
            OnboardingPane(
                title = stringResource(R.string.feature_onboarding_update_title),
                body = stringResource(R.string.feature_onboarding_update_body),
                primaryLabel = stringResource(R.string.feature_onboarding_update_action),
                onPrimary = onOpenHealthConnect,
                modifier = modifier,
            )
        }
        OnboardingStep.RequestPermissions -> {
            OnboardingPane(
                title = stringResource(R.string.feature_onboarding_permissions_title),
                body = stringResource(R.string.feature_onboarding_permissions_body),
                extra = {
                    PermissionBullets()
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.feature_onboarding_permissions_history),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (state.permissionsDenied) {
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = stringResource(R.string.feature_onboarding_permissions_denied),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                },
                primaryLabel = stringResource(R.string.feature_onboarding_permissions_action),
                onPrimary = onRequestPermissions,
                secondaryLabel = stringResource(R.string.feature_onboarding_privacy_policy),
                onSecondary = onShowPrivacyPolicy,
                tertiaryLabel = stringResource(R.string.feature_onboarding_open_settings),
                onTertiary = onOpenHealthConnect,
                modifier = modifier,
            )
        }
        OnboardingStep.LinkGoogleHealth -> {
            OnboardingPane(
                title = stringResource(R.string.feature_onboarding_google_health_title),
                body = stringResource(R.string.feature_onboarding_google_health_body),
                extra = {
                    if (state.isLinkingGoogleHealth) {
                        Text(
                            text = stringResource(R.string.feature_onboarding_google_health_linking),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                    state.googleHealthError?.let { error ->
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                },
                primaryLabel = stringResource(R.string.feature_onboarding_google_health_action),
                onPrimary = onLinkGoogleHealth,
                primaryEnabled = !state.isLinkingGoogleHealth,
                secondaryLabel = stringResource(R.string.feature_onboarding_google_health_skip),
                onSecondary = onSkipGoogleHealth,
                tertiaryLabel = stringResource(R.string.feature_onboarding_privacy_policy),
                onTertiary = onShowPrivacyPolicy,
                modifier = modifier,
            )
        }
        OnboardingStep.Ready -> {
            OnboardingPane(
                title = stringResource(R.string.feature_onboarding_ready_title),
                body = stringResource(R.string.feature_onboarding_ready_body),
                extra = {
                    if (state.googleHealthLinked) {
                        Text(
                            text = stringResource(R.string.feature_onboarding_ready_google_linked),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                },
                primaryLabel = stringResource(R.string.feature_onboarding_continue),
                onPrimary = onContinue,
                modifier = modifier,
            )
        }
    }
}

@Composable
private fun PermissionBullets() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("• ${stringResource(R.string.feature_onboarding_permissions_sleep)}")
        Text("• ${stringResource(R.string.feature_onboarding_permissions_heart_rate)}")
        Text("• ${stringResource(R.string.feature_onboarding_permissions_activity)}")
    }
}

@Composable
private fun OnboardingPane(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    extra: (@Composable () -> Unit)? = null,
    primaryLabel: String? = null,
    onPrimary: (() -> Unit)? = null,
    primaryEnabled: Boolean = true,
    secondaryLabel: String? = null,
    onSecondary: (() -> Unit)? = null,
    tertiaryLabel: String? = null,
    onTertiary: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            Text(
                text = stringResource(R.string.feature_onboarding_app_name),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.feature_onboarding_tagline),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(12.dp))
            Text(text = title, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(12.dp))
            Text(
                text = body,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (extra != null) {
                Spacer(Modifier.height(16.dp))
                extra()
            }
        }
        Column(modifier = Modifier.fillMaxWidth()) {
            if (primaryLabel != null && onPrimary != null) {
                Button(
                    onClick = onPrimary,
                    enabled = primaryEnabled,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(primaryLabel)
                }
            }
            if (secondaryLabel != null && onSecondary != null) {
                TextButton(
                    onClick = onSecondary,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(secondaryLabel)
                }
            }
            if (tertiaryLabel != null && onTertiary != null) {
                TextButton(
                    onClick = onTertiary,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(tertiaryLabel)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OnboardingPermissionsPreview() {
    FitdroidTheme(dynamicColor = false) {
        OnboardingContent(
            state = OnboardingState(
                isLoading = false,
                availability = HealthConnectAvailability.Available,
            ),
            onRequestPermissions = {},
            onOpenHealthConnect = {},
            onLinkGoogleHealth = {},
            onSkipGoogleHealth = {},
            onContinue = {},
            onShowPrivacyPolicy = {},
            onHidePrivacyPolicy = {},
        )
    }
}
