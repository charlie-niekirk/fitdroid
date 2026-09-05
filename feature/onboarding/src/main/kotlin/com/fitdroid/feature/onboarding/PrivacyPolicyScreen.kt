@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.fitdroid.feature.onboarding

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fitdroid.core.designsystem.theme.FitdroidTheme

@Composable
fun PrivacyPolicyScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
    ) {
        TextButton(onClick = onBack) {
            Text(stringResource(R.string.feature_onboarding_back))
        }
        Text(
            text = stringResource(R.string.feature_onboarding_privacy_title),
            style = MaterialTheme.typography.titleLargeEmphasized,
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.feature_onboarding_privacy_intro),
            style = MaterialTheme.typography.bodyLarge,
        )
        PrivacySection(
            title = stringResource(R.string.feature_onboarding_privacy_health_connect_title),
            body = stringResource(R.string.feature_onboarding_privacy_health_connect_body),
        )
        PrivacySection(
            title = stringResource(R.string.feature_onboarding_privacy_google_health_title),
            body = stringResource(R.string.feature_onboarding_privacy_google_health_body),
        )
        PrivacySection(
            title = stringResource(R.string.feature_onboarding_privacy_storage_title),
            body = stringResource(R.string.feature_onboarding_privacy_storage_body),
        )
        PrivacySection(
            title = stringResource(R.string.feature_onboarding_privacy_control_title),
            body = stringResource(R.string.feature_onboarding_privacy_control_body),
        )
    }
}

@Composable
private fun PrivacySection(title: String, body: String) {
    Column {
        Spacer(Modifier.height(20.dp))
        Text(text = title, style = MaterialTheme.typography.titleMediumEmphasized)
        Spacer(Modifier.height(8.dp))
        Text(
            text = body,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PrivacyPolicyScreenPreview() {
    FitdroidTheme(dynamicColor = false) {
        PrivacyPolicyScreen(onBack = {})
    }
}
