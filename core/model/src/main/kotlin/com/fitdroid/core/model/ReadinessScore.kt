package com.fitdroid.core.model

import java.time.LocalDate

data class ReadinessScoreBreakdown(
    val hrv: Int?,
    val restingHeartRate: Int,
    val sleep: Int,
    val trainingLoad: Int?,
)

data class ReadinessScore(
    val date: LocalDate,
    val score: Int,
    val breakdown: ReadinessScoreBreakdown,
    val usingDegradedModel: Boolean,
)
