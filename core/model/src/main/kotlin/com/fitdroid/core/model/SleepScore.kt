package com.fitdroid.core.model

import java.time.LocalDate

data class SleepScoreBreakdown(
    val duration: Int,
    val restorative: Int,
    val efficiency: Int,
    val disturbances: Int,
    val consistency: Int,
)

data class SleepScore(
    val date: LocalDate,
    val score: Int,
    val breakdown: SleepScoreBreakdown,
)
