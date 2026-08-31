package com.fitdroid.core.network

enum class HealthDataKind {
    Daily,
    Session,
    Sample,
    Interval,
}

enum class HealthDataType(
    val path: String,
    val kind: HealthDataKind,
    val supportsList: Boolean = true,
) {
    DailyHeartRateVariability(
        path = "daily-heart-rate-variability",
        kind = HealthDataKind.Daily,
    ),
    DailyOxygenSaturation(
        path = "daily-oxygen-saturation",
        kind = HealthDataKind.Daily,
    ),
    DailyRespiratoryRate(
        path = "daily-respiratory-rate",
        kind = HealthDataKind.Daily,
    ),
    DailyRestingHeartRate(
        path = "daily-resting-heart-rate",
        kind = HealthDataKind.Daily,
    ),
    DailySleepTemperatureDerivations(
        path = "daily-sleep-temperature-derivations",
        kind = HealthDataKind.Daily,
    ),
    RespiratoryRateSleepSummary(
        path = "respiratory-rate-sleep-summary",
        kind = HealthDataKind.Sample,
    ),
    Floors(
        path = "floors",
        kind = HealthDataKind.Interval,
        supportsList = false,
    ),
    TotalCalories(
        path = "total-calories",
        kind = HealthDataKind.Interval,
        supportsList = false,
    ),
    ActiveMinutes(
        path = "active-minutes",
        kind = HealthDataKind.Interval,
        supportsList = false,
    ),
    CaloriesInHeartRateZone(
        path = "calories-in-heart-rate-zone",
        kind = HealthDataKind.Interval,
        supportsList = false,
    ),
    TimeInHeartRateZone(
        path = "time-in-heart-rate-zone",
        kind = HealthDataKind.Interval,
        supportsList = false,
    ),
    DailyHeartRateZones(
        path = "daily-heart-rate-zones",
        kind = HealthDataKind.Daily,
        supportsList = false,
    ),
    ;

    companion object {
        val mvpPhysiological: Set<HealthDataType> = setOf(
            DailyHeartRateVariability,
            DailyOxygenSaturation,
            DailyRespiratoryRate,
            DailyRestingHeartRate,
            DailySleepTemperatureDerivations,
            RespiratoryRateSleepSummary,
        )
    }
}

class ListNotSupportedException(type: HealthDataType) : IllegalArgumentException(
    "${type.path} does not support :list. Use rollUp or dailyRollUp instead.",
)
