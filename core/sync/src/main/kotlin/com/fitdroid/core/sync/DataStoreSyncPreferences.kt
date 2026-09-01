package com.fitdroid.core.sync

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import com.fitdroid.core.network.HealthDataType
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import java.time.LocalDate
import kotlinx.coroutines.flow.first

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
class DataStoreSyncPreferences(
    context: Context,
) : SyncPreferences {
    private val dataStore: DataStore<Preferences> =
        PreferenceDataStoreFactory.create(
            produceFile = { context.applicationContext.preferencesDataStoreFile("sync_prefs") },
        )

    override suspend fun healthConnectChangesToken(): String? =
        dataStore.data.first()[HealthConnectTokenKey]

    override suspend fun saveHealthConnectChangesToken(token: String?) {
        dataStore.edit { prefs ->
            if (token.isNullOrBlank()) {
                prefs.remove(HealthConnectTokenKey)
            } else {
                prefs[HealthConnectTokenKey] = token
            }
        }
    }

    override suspend fun googleHealthWatermark(type: HealthDataType): LocalDate? =
        dataStore.data.first()[watermarkKey(type)]?.let(LocalDate::parse)

    override suspend fun saveGoogleHealthWatermark(type: HealthDataType, date: LocalDate) {
        dataStore.edit { prefs ->
            prefs[watermarkKey(type)] = date.toString()
        }
    }

    private fun watermarkKey(type: HealthDataType) =
        stringPreferencesKey("google_health_watermark_${type.path}")

    private companion object {
        val HealthConnectTokenKey = stringPreferencesKey("health_connect_changes_token")
    }
}
