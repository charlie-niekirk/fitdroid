package com.fitdroid.core.sync

import com.fitdroid.core.network.HealthDataType
import java.time.LocalDate

internal class FakeSyncPreferences : SyncPreferences {
    var healthConnectToken: String? = null
    val watermarks = mutableMapOf<HealthDataType, LocalDate>()

    override suspend fun healthConnectChangesToken(): String? = healthConnectToken

    override suspend fun saveHealthConnectChangesToken(token: String?) {
        healthConnectToken = token
    }

    override suspend fun googleHealthWatermark(type: HealthDataType): LocalDate? = watermarks[type]

    override suspend fun saveGoogleHealthWatermark(type: HealthDataType, date: LocalDate) {
        watermarks[type] = date
    }
}
