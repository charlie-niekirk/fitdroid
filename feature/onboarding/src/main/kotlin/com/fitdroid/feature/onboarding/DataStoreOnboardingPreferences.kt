package com.fitdroid.feature.onboarding

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStoreFile
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
class DataStoreOnboardingPreferences(
    context: Context,
) : OnboardingPreferences {
    private val dataStore: DataStore<Preferences> =
        PreferenceDataStoreFactory.create(
            produceFile = { context.applicationContext.preferencesDataStoreFile("onboarding_prefs") },
        )

    override val isComplete: Flow<Boolean> =
        dataStore.data.map { prefs -> prefs[CompleteKey] == true }

    override suspend fun setComplete(complete: Boolean) {
        dataStore.edit { prefs ->
            prefs[CompleteKey] = complete
        }
    }

    private companion object {
        val CompleteKey = booleanPreferencesKey("onboarding_complete")
    }
}
