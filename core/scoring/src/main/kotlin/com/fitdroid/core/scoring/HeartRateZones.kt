package com.fitdroid.core.scoring

import com.fitdroid.core.model.HeartRateSample

internal object HeartRateZones {
    const val DEFAULT_MAX_HR_BPM = 190
    const val MODERATE_FRACTION = 0.64

    fun moderateThresholdBpm(maxHrBpm: Int = DEFAULT_MAX_HR_BPM): Int =
        (maxHrBpm * MODERATE_FRACTION).toInt()

    fun cardioMinutes(
        samples: List<HeartRateSample>,
        maxHrBpm: Int = DEFAULT_MAX_HR_BPM,
    ): Double {
        if (samples.isEmpty()) return 0.0
        val threshold = moderateThresholdBpm(maxHrBpm)
        return samples
            .filter { it.bpm >= threshold }
            .sumOf { it.resolutionSeconds.coerceAtLeast(1) } / 60.0
    }
}
