package com.fitdroid.core.model

import java.time.Instant

data class RestingHeartRateSample(
    val time: Instant,
    val bpm: Long,
    val hcRecordId: String? = null,
)
