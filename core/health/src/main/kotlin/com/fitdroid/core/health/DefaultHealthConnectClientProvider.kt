package com.fitdroid.core.health

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
class DefaultHealthConnectClientProvider(
    private val context: Context,
) : HealthConnectClientProvider {
    override fun availability(): HealthConnectAvailability =
        sdkStatusToAvailability(HealthConnectClient.getSdkStatus(context))

    override fun clientOrNull(): HealthConnectClient? =
        if (availability() == HealthConnectAvailability.Available) {
            HealthConnectClient.getOrCreate(context)
        } else {
            null
        }
}
