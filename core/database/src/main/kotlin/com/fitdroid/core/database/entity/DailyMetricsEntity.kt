package com.fitdroid.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "daily_metrics")
data class DailyMetricsEntity(
    @PrimaryKey val date: LocalDate,
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
