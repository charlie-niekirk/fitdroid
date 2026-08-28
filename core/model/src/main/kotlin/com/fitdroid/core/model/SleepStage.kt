package com.fitdroid.core.model

import java.time.Duration
import java.time.Instant

data class SleepStage(
    val type: SleepStageType,
    val start: Instant,
    val end: Instant,
) {
    val duration: Duration
        get() = Duration.between(start, end)
}
