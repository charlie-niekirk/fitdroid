package com.fitdroid.core.sync

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import com.fitdroid.core.model.UserSettings
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
class DataStoreUserSettings(
    context: Context,
) : UserSettingsRepository {
    private val dataStore: DataStore<Preferences> =
        PreferenceDataStoreFactory.create(
            produceFile = { context.applicationContext.preferencesDataStoreFile("user_settings") },
        )

    override val settings: Flow<UserSettings> =
        dataStore.data.map { prefs ->
            UserSettings(
                sleepTargetMinutes = prefs[SleepTargetMinutesKey] ?: UserSettings.DefaultSleepTargetMinutes,
                steps = prefs[StepsKey] ?: UserSettings.DefaultSteps,
                activeMinutes = prefs[ActiveMinutesKey] ?: UserSettings.DefaultActiveMinutes,
                cardioMinutes = prefs[CardioMinutesKey] ?: UserSettings.DefaultCardioMinutes,
                periodicSyncEnabled = prefs[PeriodicSyncKey] ?: true,
            )
        }

    override suspend fun update(transform: (UserSettings) -> UserSettings) {
        dataStore.edit { prefs ->
            val current = UserSettings(
                sleepTargetMinutes = prefs[SleepTargetMinutesKey] ?: UserSettings.DefaultSleepTargetMinutes,
                steps = prefs[StepsKey] ?: UserSettings.DefaultSteps,
                activeMinutes = prefs[ActiveMinutesKey] ?: UserSettings.DefaultActiveMinutes,
                cardioMinutes = prefs[CardioMinutesKey] ?: UserSettings.DefaultCardioMinutes,
                periodicSyncEnabled = prefs[PeriodicSyncKey] ?: true,
            )
            val next = transform(current)
            prefs[SleepTargetMinutesKey] = next.sleepTargetMinutes
            prefs[StepsKey] = next.steps
            prefs[ActiveMinutesKey] = next.activeMinutes
            prefs[CardioMinutesKey] = next.cardioMinutes
            prefs[PeriodicSyncKey] = next.periodicSyncEnabled
        }
    }

    private companion object {
        val SleepTargetMinutesKey = intPreferencesKey("sleep_target_minutes")
        val StepsKey = longPreferencesKey("steps")
        val ActiveMinutesKey = intPreferencesKey("active_minutes")
        val CardioMinutesKey = intPreferencesKey("cardio_minutes")
        val PeriodicSyncKey = booleanPreferencesKey("periodic_sync_enabled")
    }
}
