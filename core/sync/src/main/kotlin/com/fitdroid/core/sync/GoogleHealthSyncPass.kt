package com.fitdroid.core.sync

import com.fitdroid.core.common.AppError
import com.fitdroid.core.common.result.Result
import com.fitdroid.core.model.DailyMetrics
import com.fitdroid.core.network.GoogleHealthClient
import com.fitdroid.core.network.GoogleHealthFeatureFlag
import com.fitdroid.core.network.HealthDataKind
import com.fitdroid.core.network.HealthDataType
import com.fitdroid.core.network.model.DataPoint
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId

@SingleIn(AppScope::class)
@Inject
class GoogleHealthSyncPass(
    private val client: GoogleHealthClient,
    private val featureFlag: GoogleHealthFeatureFlag,
    private val store: LocalHealthStore,
    private val preferences: SyncPreferences,
    private val clock: Clock,
    private val zoneId: ZoneId,
) {
    suspend fun sync(): SyncPassResult {
        if (!featureFlag.isEnabled()) {
            return SyncPassResult.skipped("Google Health API is disabled")
        }
        return try {
            unwrap(client.verifyLinkage())
            val today = LocalDate.now(clock.withZone(zoneId))
            val endExclusive = today.plusDays(1)
            for (type in DailyMvpTypes) {
                val start = preferences.googleHealthWatermark(type) ?: today.minusDays(LookbackDays - 1)
                val points = unwrap(client.listAllDataPoints(type, start, endExclusive))
                points.mapNotNull { it.toDailyMetrics() }.forEach { store.mergeDailyMetrics(it) }
                preferences.saveGoogleHealthWatermark(type, today)
            }
            SyncPassResult.success()
        } catch (error: SyncPassException) {
            SyncPassResult.failed(error.error.message, retryable = error.retryable)
        }
    }

    private fun <T> unwrap(result: Result<T>): T =
        when (result) {
            is Result.Success -> result.data
            is Result.Failure -> throw SyncPassException(result.error)
            Result.Loading -> throw SyncPassException(AppError.Unknown("Unexpected loading state"))
        }

    companion object {
        const val LookbackDays = 30L
        val DailyMvpTypes: List<HealthDataType> =
            HealthDataType.mvpPhysiological.filter { it.kind == HealthDataKind.Daily }
    }
}

internal fun DataPoint.toDailyMetrics(): DailyMetrics? {
    dailyHeartRateVariability?.let { value ->
        return DailyMetrics(
            date = value.date.toLocalDate(),
            hrvRmssdMs = value.deepSleepRootMeanSquareOfSuccessiveDifferencesMilliseconds
                ?: value.averageHeartRateVariabilityMilliseconds,
        )
    }
    dailyOxygenSaturation?.let { value ->
        return DailyMetrics(
            date = value.date.toLocalDate(),
            spo2Percent = value.averagePercentage,
        )
    }
    dailyRespiratoryRate?.let { value ->
        return DailyMetrics(
            date = value.date.toLocalDate(),
            respiratoryRateBrpm = value.breathsPerMinute,
        )
    }
    dailyRestingHeartRate?.let { value ->
        return DailyMetrics(
            date = value.date.toLocalDate(),
            restingHeartRateBpm = value.beatsPerMinute,
        )
    }
    dailySleepTemperatureDerivations?.let { value ->
        return DailyMetrics(
            date = value.date.toLocalDate(),
            skinTempDeviationCelsius = value.deviationCelsius,
        )
    }
    return null
}
