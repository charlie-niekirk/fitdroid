package com.fitdroid.core.model

import java.time.LocalDate

data class DailyScores(
    val date: LocalDate,
    val sleep: SleepScore?,
    val readiness: ReadinessScore?,
    val activity: ActivityScore?,
)
