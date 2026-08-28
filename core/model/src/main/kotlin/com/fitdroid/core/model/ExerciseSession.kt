package com.fitdroid.core.model

import java.time.Duration
import java.time.Instant

data class ExerciseSession(
    val id: String,
    val hcRecordId: String? = null,
    val start: Instant,
    val end: Instant,
    val activityType: String,
    val caloriesKcal: Double? = null,
) {
    val duration: Duration
        get() = Duration.between(start, end)
}
