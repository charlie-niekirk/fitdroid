package com.fitdroid.feature.onboarding

import android.content.Intent
import com.fitdroid.core.auth.AuthRepository
import com.fitdroid.core.health.HealthConnectAvailability
import com.fitdroid.core.health.HealthConnectPermissions
import com.fitdroid.core.network.GoogleHealthFeatureFlag
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.orbitmvi.orbit.test.test

class OnboardingViewModelTest {
    private val essential = HealthConnectPermissions.recordReadPermissions

    @Test
    fun load_whenHealthConnectUnavailable_showsUnavailable() = runTest {
        val viewModel = viewModel(
            health = FakeOnboardingHealthConnect(availability = HealthConnectAvailability.Unavailable),
        )
        viewModel.test(this) {
            val job = runOnCreate()
            expectState {
                copy(
                    isLoading = false,
                    availability = HealthConnectAvailability.Unavailable,
                    requestedPermissions = essential,
                )
            }
            assertEquals(OnboardingStep.Unavailable, viewModel.container.stateFlow.value.step)
            job.join()
        }
    }

    @Test
    fun load_whenUpdateRequired_showsUpdateRequired() = runTest {
        val viewModel = viewModel(
            health = FakeOnboardingHealthConnect(
                availability = HealthConnectAvailability.UpdateRequired,
            ),
        )
        viewModel.test(this) {
            runOnCreate()
            expectState {
                copy(isLoading = false, availability = HealthConnectAvailability.UpdateRequired, requestedPermissions = essential)
            }
            assertEquals(OnboardingStep.UpdateRequired, viewModel.container.stateFlow.value.step)
            cancelAndIgnoreRemainingItems()
        }
    }

    @Test
    fun load_whenAvailableWithoutPermissions_showsPermissionRequest() = runTest {
        val viewModel = viewModel()
        viewModel.test(this) {
            runOnCreate()
            expectState {
                copy(
                    isLoading = false,
                    availability = HealthConnectAvailability.Available,
                    requestedPermissions = essential,
                )
            }
            assertEquals(OnboardingStep.RequestPermissions, viewModel.container.stateFlow.value.step)
            cancelAndIgnoreRemainingItems()
        }
    }

    @Test
    fun load_whenPermissionsGrantedAndGoogleHealthOff_showsReady() = runTest {
        val viewModel = viewModel(
            health = FakeOnboardingHealthConnect(granted = essential),
        )
        viewModel.test(this) {
            runOnCreate()
            expectState {
                copy(
                    isLoading = false,
                    availability = HealthConnectAvailability.Available,
                    requestedPermissions = essential,
                    grantedPermissions = essential,
                    hasEssentialPermissions = true,
                )
            }
            assertEquals(OnboardingStep.Ready, viewModel.container.stateFlow.value.step)
            cancelAndIgnoreRemainingItems()
        }
    }

    @Test
    fun load_whenPermissionsGrantedAndGoogleHealthEnabled_showsLinkStep() = runTest {
        val viewModel = viewModel(
            health = FakeOnboardingHealthConnect(granted = essential),
            googleHealthEnabled = true,
        )
        viewModel.test(this) {
            runOnCreate()
            expectState {
                copy(
                    isLoading = false,
                    availability = HealthConnectAvailability.Available,
                    requestedPermissions = essential,
                    grantedPermissions = essential,
                    hasEssentialPermissions = true,
                    googleHealthEnabled = true,
                )
            }
            assertEquals(OnboardingStep.LinkGoogleHealth, viewModel.container.stateFlow.value.step)
            cancelAndIgnoreRemainingItems()
        }
    }

    @Test
    fun load_whenAlreadyCompleteWithPermissions_postsCompleted() = runTest {
        val viewModel = viewModel(
            health = FakeOnboardingHealthConnect(granted = essential),
            preferences = FakeOnboardingPreferences(initiallyComplete = true),
        )
        viewModel.test(this) {
            runOnCreate()
            expectState {
                copy(
                    isLoading = false,
                    availability = HealthConnectAvailability.Available,
                    requestedPermissions = essential,
                    grantedPermissions = essential,
                    hasEssentialPermissions = true,
                    isComplete = true,
                )
            }
            expectSideEffect(OnboardingEffect.Completed)
        }
    }

    @Test
    fun requestPermissions_postsPermissionSideEffect() = runTest {
        val viewModel = viewModel()
        viewModel.test(
            this,
            OnboardingState(
                isLoading = false,
                availability = HealthConnectAvailability.Available,
                requestedPermissions = essential,
            ),
        ) {
            containerHost.requestPermissions()
            expectSideEffect(OnboardingEffect.RequestHealthConnectPermissions(essential))
        }
    }

    @Test
    fun onPermissionsResult_whenEssentialGrantedAndGoogleHealthOn_movesToLinkStep() = runTest {
        val viewModel = viewModel(googleHealthEnabled = true)
        viewModel.test(
            this,
            OnboardingState(
                isLoading = false,
                availability = HealthConnectAvailability.Available,
                requestedPermissions = essential,
            ),
        ) {
            containerHost.onPermissionsResult(essential)
            expectState {
                copy(
                    grantedPermissions = essential,
                    hasEssentialPermissions = true,
                    googleHealthEnabled = true,
                    permissionsDenied = false,
                )
            }
            assertEquals(OnboardingStep.LinkGoogleHealth, viewModel.container.stateFlow.value.step)
            cancelAndIgnoreRemainingItems()
        }
    }

    @Test
    fun onPermissionsResult_whenDenied_staysOnPermissionsWithError() = runTest {
        val viewModel = viewModel()
        viewModel.test(
            this,
            OnboardingState(
                isLoading = false,
                availability = HealthConnectAvailability.Available,
                requestedPermissions = essential,
            ),
        ) {
            containerHost.onPermissionsResult(emptySet())
            expectState { copy(permissionsDenied = true, grantedPermissions = emptySet()) }
            assertEquals(OnboardingStep.RequestPermissions, viewModel.container.stateFlow.value.step)
            cancelAndIgnoreRemainingItems()
        }
    }

    @Test
    fun skipGoogleHealth_marksCompleteAndFinishes() = runTest {
        val preferences = FakeOnboardingPreferences()
        val viewModel = viewModel(
            health = FakeOnboardingHealthConnect(granted = essential),
            googleHealthEnabled = true,
            preferences = preferences,
        )
        viewModel.test(
            this,
            OnboardingState(
                isLoading = false,
                availability = HealthConnectAvailability.Available,
                grantedPermissions = essential,
                hasEssentialPermissions = true,
                googleHealthEnabled = true,
            ),
        ) {
            containerHost.skipGoogleHealth()
            expectState { copy(isComplete = true) }
            expectSideEffect(OnboardingEffect.Completed)
        }
        assertTrue(preferences.complete.value)
    }

    @Test
    fun continueToApp_marksCompleteAndFinishes() = runTest {
        val preferences = FakeOnboardingPreferences()
        val viewModel = viewModel(preferences = preferences)
        viewModel.test(
            this,
            OnboardingState(
                isLoading = false,
                availability = HealthConnectAvailability.Available,
                grantedPermissions = essential,
                hasEssentialPermissions = true,
            ),
        ) {
            containerHost.continueToApp()
            expectState { copy(isComplete = true) }
            expectSideEffect(OnboardingEffect.Completed)
        }
        assertTrue(preferences.complete.value)
    }

    @Test
    fun linkGoogleHealth_whenOAuthConfigured_launchesAuthIntent() = runTest {
        val auth = FakeAuthRepository()
        val authIntent = Intent("com.fitdroid.test.AUTH")
        auth.authorizationIntent = authIntent
        val viewModel = viewModel(auth = auth, googleHealthEnabled = true)
        viewModel.test(
            this,
            OnboardingState(
                isLoading = false,
                hasEssentialPermissions = true,
                googleHealthEnabled = true,
            ),
        ) {
            containerHost.linkGoogleHealth()
            expectState { copy(isLinkingGoogleHealth = true) }
            val effect = awaitSideEffect() as OnboardingEffect.LaunchGoogleHealthAuth
            assertEquals(authIntent, effect.intent)
            cancelAndIgnoreRemainingItems()
        }
    }

    @Test
    fun openHealthConnect_whenUpdateRequired_opensProviderUpdate() = runTest {
        val viewModel = viewModel()
        viewModel.test(
            this,
            OnboardingState(
                isLoading = false,
                availability = HealthConnectAvailability.UpdateRequired,
            ),
        ) {
            containerHost.openHealthConnect()
            expectSideEffect(OnboardingEffect.OpenProviderUpdate)
        }
    }

    private fun viewModel(
        health: FakeOnboardingHealthConnect = FakeOnboardingHealthConnect(),
        auth: FakeAuthRepository = FakeAuthRepository(),
        googleHealthEnabled: Boolean = false,
        preferences: FakeOnboardingPreferences = FakeOnboardingPreferences(),
    ) = OnboardingViewModel(
        healthConnect = health,
        authRepository = auth,
        googleHealthFeatureFlag = GoogleHealthFeatureFlag { googleHealthEnabled },
        onboardingPreferences = preferences,
    )
}

private class FakeOnboardingPreferences(
    initiallyComplete: Boolean = false,
) : OnboardingPreferences {
    val complete = MutableStateFlow(initiallyComplete)
    override val isComplete = complete
    override suspend fun setComplete(complete: Boolean) {
        this.complete.value = complete
    }
}

private class FakeAuthRepository : AuthRepository {
    override val isAuthorized = MutableStateFlow(false)
    var authorizationIntent: Intent = Intent("com.fitdroid.test.AUTH")
    var authorizationError: Throwable? = null

    override fun authorizationIntent(): Intent = authorizationIntent

    override suspend fun onAuthorizationResult(intent: Intent) {
        authorizationError?.let { throw it }
        isAuthorized.value = true
    }

    override suspend fun signOut() {
        isAuthorized.value = false
    }

    override suspend fun accessToken(forceRefresh: Boolean): String = "token"
}
