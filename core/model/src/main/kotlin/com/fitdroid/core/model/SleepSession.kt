package com.fitdroid.core.model

import java.time.Duration
import java.time.Instant

data class SleepSession(
    val id: String,
    val hcRecordId: String? = null,
    val hcLastModified: Instant? = null,
    val start: Instant,
    val end: Instant,
    val stages: List<SleepStage> = emptyList(),
    val notes: String? = null,
) {
    val timeInBed: Duration
        get() = Duration.between(start, end)

    val asleepDuration: Duration
        get() = stages
            .filter {
                it.type == SleepStageType.Light ||
                    it.type == SleepStageType.Deep ||
                    it.type == SleepStageType.Rem
            }
            .fold(Duration.ZERO) { acc, stage -> acc + stage.duration }

    val restorativeDuration: Duration
        get() = stages
            .filter { it.type == SleepStageType.Deep || it.type == SleepStageType.Rem }
            .fold(Duration.ZERO) { acc, stage -> acc + stage.duration }
}
