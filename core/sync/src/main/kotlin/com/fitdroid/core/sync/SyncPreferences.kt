package com.fitdroid.core.sync

import com.fitdroid.core.network.HealthDataType
import java.time.LocalDate

interface SyncPreferences {
    suspend fun healthConnectChangesToken(): String?

    suspend fun saveHealthConnectChangesToken(token: String?)

    suspend fun googleHealthWatermark(type: HealthDataType): LocalDate?

    suspend fun saveGoogleHealthWatermark(type: HealthDataType, date: LocalDate)
}
