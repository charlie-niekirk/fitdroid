package com.fitdroid.core.health

import androidx.health.connect.client.records.SleepSessionRecord
import com.fitdroid.core.model.HeartRateSample
import com.fitdroid.core.model.SleepSession
import com.fitdroid.core.model.SleepStageType
import java.time.Duration
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HealthConnectMappersTest {
    @Test
    fun sleepStageType_mapsHealthConnectConstants() {
        assertEquals(SleepStageType.Awake, sleepStageType(SleepSessionRecord.STAGE_TYPE_AWAKE))
        assertEquals(SleepStageType.Awake, sleepStageType(SleepSessionRecord.STAGE_TYPE_OUT_OF_BED))
        assertEquals(SleepStageType.Light, sleepStageType(SleepSessionRecord.STAGE_TYPE_LIGHT))
        assertEquals(SleepStageType.Deep, sleepStageType(SleepSessionRecord.STAGE_TYPE_DEEP))
        assertEquals(SleepStageType.Rem, sleepStageType(SleepSessionRecord.STAGE_TYPE_REM))
        assertEquals(SleepStageType.Unknown, sleepStageType(-1))
    }
}

class HeartRateDownsamplerTest {
    @Test
    fun downsample_keepsSleepWindowSamplesAndAveragesAwake() {
        val nightStart = Instant.parse("2026-08-28T00:00:00Z")
        val nightEnd = Instant.parse("2026-08-28T01:00:00Z")
        val sleep = SleepSession(
            id = "sleep",
            start = nightStart,
            end = nightEnd,
        )
        val samples = listOf(
            HeartRateSample(nightStart.plusSeconds(10), 52),
            HeartRateSample(nightStart.plusSeconds(15), 54),
            HeartRateSample(nightEnd.plusSeconds(10), 80),
            HeartRateSample(nightEnd.plusSeconds(20), 90),
        )

        val result = HeartRateDownsampler.downsample(
            samples = samples,
            sleepSessions = listOf(sleep),
            awakeResolution = Duration.ofMinutes(1),
        )

        assertEquals(3, result.size)
        assertEquals(52L, result[0].bpm)
        assertEquals(54L, result[1].bpm)
        assertEquals(85L, result[2].bpm)
        assertTrue(result[2].resolutionSeconds >= 60)
    }
}
