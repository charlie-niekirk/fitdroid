package com.fitdroid.feature.settings

import android.content.Intent
import com.fitdroid.core.auth.AuthRepository
import com.fitdroid.core.database.SyncStatusRepository
import com.fitdroid.core.model.SyncState
import com.fitdroid.core.model.UserSettings
import com.fitdroid.core.network.GoogleHealthFeatureFlag
import com.fitdroid.core.sync.ImmediateSync
import com.fitdroid.core.sync.ScoreRefresh
import com.fitdroid.core.sync.UserSettingsRepository
import java.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import org.orbitmvi.orbit.test.test

class SettingsViewModelTest {
    @Test
    fun collect_projectsGoalsAndSyncStatus() = runTest {
        val settings = FakeUserSettingsRepository()
        val syncStatus = FakeSyncStatusRepository(
            SyncState(
                source = SyncState.SOURCE_HEALTH_CONNECT,
                lastSuccessAt = Instant.parse("2026-09-02T10:14:00Z"),
            ),
        )
        val viewModel = viewModel(settings = settings, syncStatus = syncStatus)
        viewModel.test(this) {
            runOnCreate()
            expectState {
                copy(
                    isLoading = false,
                    settings = UserSettings.Default,
                    healthConnect = syncStatus.states.value.first(),
                    googleHealthEnabled = false,
                    googleHealthLinked = false,
                )
            }
            cancelAndIgnoreRemainingItems()
        }
    }

    @Test
    fun adjustGoal_stepsUp_persistsAndRescores() = runTest {
        val settings = FakeUserSettingsRepository()
        val scores = FakeScoreRefresh()
        val viewModel = viewModel(settings = settings, scoreRefresh = scores)
        viewModel.test(this, SettingsState(isLoading = false)) {
            containerHost.adjustGoal(GoalField.Steps, 1)
            expectState {
                copy(settings = UserSettings.Default.copy(steps = 10_500))
            }
            cancelAndIgnoreRemainingItems()
        }
        assertEquals(10_500L, settings.settings.value.steps)
        assertEquals(1, scores.refreshes)
    }

    @Test
    fun adjustGoal_sleepClampsAtMinimum() {
        val next = UserSettings(sleepTargetMinutes = UserSettings.MinSleepTargetMinutes)
            .adjust(GoalField.SleepTarget, -1)
        assertEquals(UserSettings.MinSleepTargetMinutes, next.sleepTargetMinutes)
    }

    @Test
    fun setPeriodicSyncEnabled_updatesPreference() = runTest {
        val settings = FakeUserSettingsRepository()
        val viewModel = viewModel(settings = settings)
        viewModel.test(this, SettingsState(isLoading = false)) {
            containerHost.setPeriodicSyncEnabled(false)
            expectState { copy(settings = UserSettings.Default.copy(periodicSyncEnabled = false)) }
            cancelAndIgnoreRemainingItems()
        }
        assertFalse(settings.settings.value.periodicSyncEnabled)
    }

    @Test
    fun setUseClassicHypnogram_updatesPreference() = runTest {
        val settings = FakeUserSettingsRepository()
        val viewModel = viewModel(settings = settings)
        viewModel.test(this, SettingsState(isLoading = false)) {
            containerHost.setUseClassicHypnogram(true)
            expectState { copy(settings = UserSettings.Default.copy(useClassicHypnogram = true)) }
            cancelAndIgnoreRemainingItems()
        }
        assertEquals(true, settings.settings.value.useClassicHypnogram)
    }

    @Test
    fun syncNow_requestsImmediateSync() = runTest {
        val sync = FakeImmediateSync()
        val viewModel = viewModel(sync = sync)
        viewModel.test(this, SettingsState(isLoading = false)) {
            containerHost.syncNow()
            expectState { copy(isSyncing = true) }
            cancelAndIgnoreRemainingItems()
        }
        assertEquals(1, sync.requests)
    }

    @Test
    fun linkGoogleHealth_postsAuthIntent() = runTest {
        val auth = FakeAuthRepository()
        val intent = Intent("com.fitdroid.test.AUTH")
        auth.authorizationIntent = intent
        val viewModel = viewModel(auth = auth, googleEnabled = true)
        viewModel.test(
            this,
            SettingsState(isLoading = false, googleHealthEnabled = true),
        ) {
            containerHost.linkGoogleHealth()
            expectState { copy(isLinkingGoogleHealth = true) }
            val effect = awaitSideEffect() as SettingsEffect.LaunchGoogleHealthAuth
            assertEquals(intent, effect.intent)
            cancelAndIgnoreRemainingItems()
        }
    }

    @Test
    fun unlinkGoogleHealth_signsOut() = runTest {
        val auth = FakeAuthRepository(initiallyLinked = true)
        val viewModel = viewModel(auth = auth)
        viewModel.test(
            this,
            SettingsState(isLoading = false, googleHealthLinked = true),
        ) {
            containerHost.unlinkGoogleHealth()
            expectState { copy(googleHealthLinked = false) }
            cancelAndIgnoreRemainingItems()
        }
        assertFalse(auth.isAuthorized.value)
    }

    @Test
    fun openHealthConnect_postsSettingsEffect() = runTest {
        val viewModel = viewModel()
        viewModel.test(this, SettingsState(isLoading = false)) {
            containerHost.openHealthConnect()
            expectSideEffect(SettingsEffect.OpenHealthConnectSettings)
        }
    }

    private fun viewModel(
        settings: FakeUserSettingsRepository = FakeUserSettingsRepository(),
        syncStatus: FakeSyncStatusRepository = FakeSyncStatusRepository(),
        sync: FakeImmediateSync = FakeImmediateSync(),
        scoreRefresh: FakeScoreRefresh = FakeScoreRefresh(),
        auth: FakeAuthRepository = FakeAuthRepository(),
        googleEnabled: Boolean = false,
    ) = SettingsViewModel(
        settingsRepository = settings,
        syncStatus = syncStatus,
        sync = sync,
        scoreRefresh = scoreRefresh,
        authRepository = auth,
        googleHealthFeatureFlag = GoogleHealthFeatureFlag { googleEnabled },
    )
}

private class FakeUserSettingsRepository(
    initial: UserSettings = UserSettings.Default,
) : UserSettingsRepository {
    override val settings = MutableStateFlow(initial)
    override suspend fun update(transform: (UserSettings) -> UserSettings) {
        settings.value = transform(settings.value)
    }
}

private class FakeSyncStatusRepository(
    vararg states: SyncState,
) : SyncStatusRepository {
    val states = MutableStateFlow(states.toList())
    override fun observe(): Flow<List<SyncState>> = states
}

private class FakeImmediateSync : ImmediateSync {
    var requests = 0
    override fun request() {
        requests++
    }
}

private class FakeScoreRefresh : ScoreRefresh {
    var refreshes = 0
    override suspend fun refresh() {
        refreshes++
    }
}

private class FakeAuthRepository(
    initiallyLinked: Boolean = false,
) : AuthRepository {
    override val isAuthorized = MutableStateFlow(initiallyLinked)
    var authorizationIntent: Intent = Intent("com.fitdroid.test.AUTH")

    override fun authorizationIntent(): Intent = authorizationIntent

    override suspend fun onAuthorizationResult(intent: Intent) {
        isAuthorized.value = true
    }

    override suspend fun signOut() {
        isAuthorized.value = false
    }

    override suspend fun accessToken(forceRefresh: Boolean): String = "token"
}
