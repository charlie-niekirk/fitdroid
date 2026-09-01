package com.fitdroid.feature.onboarding

import android.content.Intent
import com.fitdroid.core.health.HealthConnectAvailability

data class OnboardingState(
    val isLoading: Boolean = true,
    val availability: HealthConnectAvailability = HealthConnectAvailability.Unavailable,
    val requestedPermissions: Set<String> = emptySet(),
    val grantedPermissions: Set<String> = emptySet(),
    val hasEssentialPermissions: Boolean = false,
    val googleHealthEnabled: Boolean = false,
    val googleHealthLinked: Boolean = false,
    val isLinkingGoogleHealth: Boolean = false,
    val googleHealthError: String? = null,
    val showPrivacyPolicy: Boolean = false,
    val permissionsDenied: Boolean = false,
    val isComplete: Boolean = false,
) {
    val step: OnboardingStep
        get() = when {
            isLoading -> OnboardingStep.Loading
            showPrivacyPolicy -> OnboardingStep.PrivacyPolicy
            availability == HealthConnectAvailability.Unavailable -> OnboardingStep.Unavailable
            availability == HealthConnectAvailability.UpdateRequired -> OnboardingStep.UpdateRequired
            !hasEssentialPermissions -> OnboardingStep.RequestPermissions
            googleHealthEnabled && !googleHealthLinked && !isComplete -> OnboardingStep.LinkGoogleHealth
            else -> OnboardingStep.Ready
        }
}

enum class OnboardingStep {
    Loading,
    Unavailable,
    UpdateRequired,
    RequestPermissions,
    LinkGoogleHealth,
    Ready,
    PrivacyPolicy,
}

sealed interface OnboardingEffect {
    data class RequestHealthConnectPermissions(val permissions: Set<String>) : OnboardingEffect
    data class LaunchGoogleHealthAuth(val intent: Intent) : OnboardingEffect
    data object OpenHealthConnectSettings : OnboardingEffect
    data object OpenProviderUpdate : OnboardingEffect
    data object Completed : OnboardingEffect
}
