package com.fitdroid.core.health

import androidx.health.connect.client.HealthConnectClient

interface HealthConnectClientProvider {
    fun availability(): HealthConnectAvailability

    fun clientOrNull(): HealthConnectClient?
}

internal fun sdkStatusToAvailability(status: Int): HealthConnectAvailability =
    when (status) {
        HealthConnectClient.SDK_AVAILABLE -> HealthConnectAvailability.Available

        HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED ->
            HealthConnectAvailability.UpdateRequired

        else -> HealthConnectAvailability.Unavailable
    }
