package com.fitdroid.core.health

import com.fitdroid.core.model.HeartRateSample
import com.fitdroid.core.model.SleepSession
import java.time.Duration
import java.time.Instant

object HeartRateDownsampler {
    fun downsample(
        samples: List<HeartRateSample>,
        sleepSessions: List<SleepSession> = emptyList(),
        awakeResolution: Duration = Duration.ofMinutes(1),
    ): List<HeartRateSample> {
        if (samples.isEmpty()) return emptyList()
        val sleepWindows = sleepSessions.map { it.start to it.end }
        val inSleep = mutableListOf<HeartRateSample>()
        val awake = mutableListOf<HeartRateSample>()
        for (sample in samples.sortedBy { it.time }) {
            if (sleepWindows.any { sample.time >= it.first && sample.time < it.second }) {
                inSleep += sample
            } else {
                awake += sample
            }
        }
        return (inSleep + averageByBucket(awake, awakeResolution)).sortedBy { it.time }
    }

    private fun averageByBucket(
        samples: List<HeartRateSample>,
        resolution: Duration,
    ): List<HeartRateSample> {
        if (samples.isEmpty()) return emptyList()
        val bucketMillis = resolution.toMillis().coerceAtLeast(1L)
        return samples
            .groupBy { (it.time.toEpochMilli() / bucketMillis) * bucketMillis }
            .map { (bucketStart, group) ->
                HeartRateSample(
                    time = Instant.ofEpochMilli(bucketStart),
                    bpm = group.map { it.bpm }.average().toLong(),
                    resolutionSeconds = (bucketMillis / 1000L).toInt().coerceAtLeast(1),
                )
            }
    }
}
