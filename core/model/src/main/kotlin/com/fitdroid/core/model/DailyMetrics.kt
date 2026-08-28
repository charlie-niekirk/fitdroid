package com.fitdroid.core.model

import java.time.LocalDate

data class DailyMetrics(
    val date: LocalDate,
    val restingHeartRateBpm: Long? = null,
    val hrvRmssdMs: Double? = null,
    val spo2Percent: Double? = null,
    val respiratoryRateBrpm: Double? = null,
    val skinTempDeviationCelsius: Double? = null,
    val steps: Long? = null,
    val caloriesKcal: Double? = null,
    val distanceMeters: Double? = null,
    val exerciseMinutes: Int? = null,
)
