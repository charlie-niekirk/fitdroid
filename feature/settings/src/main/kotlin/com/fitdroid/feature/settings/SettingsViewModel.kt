package com.fitdroid.feature.settings

import android.content.Intent
import androidx.lifecycle.ViewModel
import com.fitdroid.core.auth.AuthRepository
import com.fitdroid.core.database.SyncStatusRepository
import com.fitdroid.core.network.GoogleHealthFeatureFlag
import com.fitdroid.core.sync.ImmediateSync
import com.fitdroid.core.sync.ScoreRefresh
import com.fitdroid.core.sync.UserSettingsRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.flow.combine
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.viewmodel.orbitContainer

@Inject
@ViewModelKey
@ContributesIntoMap(AppScope::class, binding<ViewModel>())
class SettingsViewModel(
    private val settingsRepository: UserSettingsRepository,
    private val syncStatus: SyncStatusRepository,
    private val sync: ImmediateSync,
    private val scoreRefresh: ScoreRefresh,
    private val authRepository: AuthRepository,
    private val googleHealthFeatureFlag: GoogleHealthFeatureFlag,
) : ViewModel(), OrbitContainerHost<SettingsState, SettingsState, SettingsEffect> {

    override val container = orbitContainer<SettingsState, SettingsEffect>(
        initialState = SettingsState(),
        onCreate = {
            reduce { state.copy(googleHealthEnabled = googleHealthFeatureFlag.isEnabled()) }
            collectSettings()
        },
    )

    fun adjustGoal(field: GoalField, direction: Int) = intent {
        val next = state.settings.adjust(field, direction)
        settingsRepository.update { next }
        reduce { state.copy(settings = next) }
        scoreRefresh.refresh()
    }

    fun setPeriodicSyncEnabled(enabled: Boolean) = intent {
        settingsRepository.update { it.copy(periodicSyncEnabled = enabled) }
        reduce { state.copy(settings = state.settings.copy(periodicSyncEnabled = enabled)) }
    }

    fun syncNow() = intent {
        reduce { state.copy(isSyncing = true) }
        sync.request()
    }

    fun openHealthConnect() = intent {
        postSideEffect(SettingsEffect.OpenHealthConnectSettings)
    }

    fun linkGoogleHealth() = intent {
        reduce { state.copy(isLinkingGoogleHealth = true, googleHealthError = null) }
        try {
            postSideEffect(SettingsEffect.LaunchGoogleHealthAuth(authRepository.authorizationIntent()))
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
                    googleHealthError = error.message ?: "Google Health sign-in failed",
                )
            }
        }
    }

    fun onAuthorizationCancelled() = intent {
        reduce { state.copy(isLinkingGoogleHealth = false) }
    }

    fun unlinkGoogleHealth() = intent {
        authRepository.signOut()
        reduce { state.copy(googleHealthLinked = false, googleHealthError = null) }
    }

    private suspend fun collectSettings() = subIntent {
        repeatOnSubscription {
            combine(
                settingsRepository.settings,
                syncStatus.observe(),
                authRepository.isAuthorized,
            ) { userSettings, syncStates, linked ->
                SettingsState(
                    isLoading = false,
                    settings = userSettings,
                    healthConnect = syncStates.healthConnect(),
                    googleHealth = syncStates.googleHealth(),
                    googleHealthEnabled = googleHealthFeatureFlag.isEnabled(),
                    googleHealthLinked = linked,
                    isLinkingGoogleHealth = state.isLinkingGoogleHealth,
                    googleHealthError = state.googleHealthError,
                    isSyncing = false,
                )
            }.collect { next ->
                reduce { next }
            }
        }
    }
}
