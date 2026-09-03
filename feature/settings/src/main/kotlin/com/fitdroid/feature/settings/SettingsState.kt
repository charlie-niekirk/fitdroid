package com.fitdroid.feature.settings

import android.content.Intent
import com.fitdroid.core.model.SyncState
import com.fitdroid.core.model.UserSettings
import java.time.Instant

data class SettingsState(
    val isLoading: Boolean = true,
    val settings: UserSettings = UserSettings.Default,
    val healthConnect: SyncState? = null,
    val googleHealth: SyncState? = null,
    val googleHealthEnabled: Boolean = false,
    val googleHealthLinked: Boolean = false,
    val isLinkingGoogleHealth: Boolean = false,
    val googleHealthError: String? = null,
    val isSyncing: Boolean = false,
)

sealed interface SettingsEffect {
    data class LaunchGoogleHealthAuth(val intent: Intent) : SettingsEffect
    data object OpenHealthConnectSettings : SettingsEffect
}

enum class GoalField {
    SleepTarget,
    Steps,
    ActiveMinutes,
    CardioMinutes,
}

internal fun UserSettings.adjust(field: GoalField, direction: Int): UserSettings = when (field) {
    GoalField.SleepTarget -> copy(
        sleepTargetMinutes = (sleepTargetMinutes + direction * UserSettings.SleepTargetStepMinutes)
            .coerceIn(UserSettings.MinSleepTargetMinutes, UserSettings.MaxSleepTargetMinutes),
    )

    GoalField.Steps -> copy(
        steps = (steps + direction * UserSettings.StepsStep)
            .coerceIn(UserSettings.MinSteps, UserSettings.MaxSteps),
    )

    GoalField.ActiveMinutes -> copy(
        activeMinutes = (activeMinutes + direction * UserSettings.ActiveMinutesStep)
            .coerceIn(UserSettings.MinActiveMinutes, UserSettings.MaxActiveMinutes),
    )

    GoalField.CardioMinutes -> copy(
        cardioMinutes = (cardioMinutes + direction * UserSettings.CardioMinutesStep)
            .coerceIn(UserSettings.MinCardioMinutes, UserSettings.MaxCardioMinutes),
    )
}

internal fun List<SyncState>.healthConnect(): SyncState? =
    firstOrNull { it.source == SyncState.SOURCE_HEALTH_CONNECT }

internal fun List<SyncState>.googleHealth(): SyncState? =
    firstOrNull { it.source == SyncState.SOURCE_GOOGLE_HEALTH }

internal fun SyncState.displayInstant(): Instant? = lastSuccessAt ?: lastAttemptAt
