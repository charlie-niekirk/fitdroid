package com.fitdroid.feature.onboarding

import android.content.Intent
import androidx.lifecycle.ViewModel
import com.fitdroid.core.auth.AuthRepository
import com.fitdroid.core.common.result.Result
import com.fitdroid.core.health.HealthConnectAvailability
import com.fitdroid.core.health.HealthConnectDataSource
import com.fitdroid.core.health.HealthConnectPermissions
import com.fitdroid.core.network.GoogleHealthFeatureFlag
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.flow.first
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.viewmodel.orbitContainer

@Inject
@ViewModelKey
@ContributesIntoMap(AppScope::class, binding<ViewModel>())
class OnboardingViewModel(
    private val healthConnect: HealthConnectDataSource,
    private val authRepository: AuthRepository,
    private val googleHealthFeatureFlag: GoogleHealthFeatureFlag,
    private val onboardingPreferences: OnboardingPreferences,
) : ViewModel(), OrbitContainerHost<OnboardingState, OnboardingState, OnboardingEffect> {

    override val container = orbitContainer<OnboardingState, OnboardingEffect>(OnboardingState()) {
        load()
    }

    fun refresh() = intent { load() }

    fun requestPermissions() = intent {
        val permissions = state.requestedPermissions.ifEmpty { healthConnect.requiredPermissions() }
        postSideEffect(OnboardingEffect.RequestHealthConnectPermissions(permissions))
    }

    fun onPermissionsResult(granted: Set<String>) = intent {
        applyLoadedState(
            availability = HealthConnectAvailability.Available,
            requested = state.requestedPermissions.ifEmpty { healthConnect.requiredPermissions() },
            granted = granted,
            googleEnabled = googleHealthFeatureFlag.isEnabled(),
            googleLinked = authRepository.isAuthorized.value,
            complete = state.isComplete,
            permissionsDenied = !HealthConnectPermissions.hasEssentialAccess(granted),
        )
    }

    fun linkGoogleHealth() = intent {
        reduce { state.copy(isLinkingGoogleHealth = true, googleHealthError = null) }
        try {
            postSideEffect(OnboardingEffect.LaunchGoogleHealthAuth(authRepository.authorizationIntent()))
        } catch (error: Exception) {
            reduce {
                state.copy(
                    isLinkingGoogleHealth = false,
                    googleHealthError = error.message ?: "Could not start Google Health sign-in",
                )
            }
        }
    }

    fun onAuthorizationResult(resultIntent: Intent) = intent {
        try {
            authRepository.onAuthorizationResult(resultIntent)
            reduce {
                state.copy(
                    googleHealthLinked = authRepository.isAuthorized.value,
                    isLinkingGoogleHealth = false,
                    googleHealthError = null,
                )
            }
        } catch (error: Exception) {
            reduce {
                state.copy(
                    isLinkingGoogleHealth = false,
                    googleHealthError = error.message ?: "Google Health linking failed",
                )
            }
        }
    }

    fun onAuthorizationCancelled() = intent {
        reduce { state.copy(isLinkingGoogleHealth = false) }
    }

    fun skipGoogleHealth() = intent {
        completeOnboarding()
    }

    fun continueToApp() = intent {
        completeOnboarding()
    }

    fun showPrivacyPolicy() = intent {
        reduce { state.copy(showPrivacyPolicy = true) }
    }

    fun hidePrivacyPolicy() = intent {
        reduce { state.copy(showPrivacyPolicy = false) }
    }

    fun openHealthConnect() = intent {
        postSideEffect(
            if (state.availability == HealthConnectAvailability.UpdateRequired) {
                OnboardingEffect.OpenProviderUpdate
            } else {
                OnboardingEffect.OpenHealthConnectSettings
            },
        )
    }

    private suspend fun completeOnboarding() = subIntent {
        onboardingPreferences.setComplete(true)
        reduce { state.copy(isComplete = true, isLinkingGoogleHealth = false, googleHealthError = null) }
        postSideEffect(OnboardingEffect.Completed)
    }

    private suspend fun load() = subIntent {
        val availability = healthConnect.availability()
        val requested = healthConnect.requiredPermissions()
        val granted = when (val result = healthConnect.grantedPermissions()) {
            is Result.Success -> result.data
            else -> emptySet()
        }
        applyLoadedState(
            availability = availability,
            requested = requested,
            granted = granted,
            googleEnabled = googleHealthFeatureFlag.isEnabled(),
            googleLinked = authRepository.isAuthorized.value,
            complete = onboardingPreferences.isComplete.first(),
        )
    }

    private suspend fun applyLoadedState(
        availability: HealthConnectAvailability,
        requested: Set<String>,
        granted: Set<String>,
        googleEnabled: Boolean,
        googleLinked: Boolean,
        complete: Boolean,
        permissionsDenied: Boolean = false,
    ) = subIntent {
        val essential = HealthConnectPermissions.hasEssentialAccess(granted)
        reduce {
            state.copy(
                isLoading = false,
                availability = availability,
                requestedPermissions = requested,
                grantedPermissions = granted,
                hasEssentialPermissions = essential,
                googleHealthEnabled = googleEnabled,
                googleHealthLinked = googleLinked,
                isLinkingGoogleHealth = false,
                isComplete = complete,
                permissionsDenied = (permissionsDenied || state.permissionsDenied) && !essential,
            )
        }
        if (essential && complete) {
            postSideEffect(OnboardingEffect.Completed)
        }
    }
}
