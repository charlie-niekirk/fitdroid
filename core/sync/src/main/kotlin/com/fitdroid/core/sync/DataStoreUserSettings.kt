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

    override val settings: Flow<UserSettings> = dataStore.data.map { it.toUserSettings() }

    override suspend fun update(transform: (UserSettings) -> UserSettings) {
        dataStore.edit { prefs ->
            val current = prefs.toUserSettings()
            val next = transform(current)
            prefs[SleepTargetMinutesKey] = next.sleepTargetMinutes
            prefs[StepsKey] = next.steps
            prefs[ActiveMinutesKey] = next.activeMinutes
            prefs[CardioMinutesKey] = next.cardioMinutes
            prefs[PeriodicSyncKey] = next.periodicSyncEnabled
            prefs[ClassicHypnogramKey] = next.useClassicHypnogram
        }
    }

    private fun Preferences.toUserSettings(): UserSettings =
        UserSettings(
            sleepTargetMinutes = this[SleepTargetMinutesKey] ?: UserSettings.DefaultSleepTargetMinutes,
            steps = this[StepsKey] ?: UserSettings.DefaultSteps,
            activeMinutes = this[ActiveMinutesKey] ?: UserSettings.DefaultActiveMinutes,
            cardioMinutes = this[CardioMinutesKey] ?: UserSettings.DefaultCardioMinutes,
            periodicSyncEnabled = this[PeriodicSyncKey] ?: true,
            useClassicHypnogram = this[ClassicHypnogramKey] ?: false,
        )

    private companion object {
        val SleepTargetMinutesKey = intPreferencesKey("sleep_target_minutes")
        val StepsKey = longPreferencesKey("steps")
        val ActiveMinutesKey = intPreferencesKey("active_minutes")
        val CardioMinutesKey = intPreferencesKey("cardio_minutes")
        val PeriodicSyncKey = booleanPreferencesKey("periodic_sync_enabled")
        val ClassicHypnogramKey = booleanPreferencesKey("use_classic_hypnogram")
    }
}
