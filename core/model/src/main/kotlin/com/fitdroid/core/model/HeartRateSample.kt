package com.fitdroid.core.model

import java.time.Instant

data class HeartRateSample(
    val time: Instant,
    val bpm: Long,
    val hcRecordId: String? = null,
    val resolutionSeconds: Int = 1,
)
