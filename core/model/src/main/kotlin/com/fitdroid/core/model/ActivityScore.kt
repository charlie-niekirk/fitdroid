package com.fitdroid.core.model

import java.time.LocalDate

data class ActivityScoreBreakdown(
    val steps: Int,
    val activeMinutes: Int,
    val cardioLoad: Int,
)

data class ActivityScore(
    val date: LocalDate,
    val score: Int,
    val breakdown: ActivityScoreBreakdown,
)
