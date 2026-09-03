package com.fitdroid.core.health

import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.Record
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.records.metadata.Metadata
import androidx.health.connect.client.units.Energy
import androidx.health.connect.client.units.Length
import com.fitdroid.core.model.HeartRateSample
import com.fitdroid.core.model.SleepSession
import com.fitdroid.core.model.SleepStageType
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HealthConnectMappersTest {
    @Test
    fun sleepStageType_mapsHealthConnectConstants() {
        assertEquals(SleepStageType.Awake, sleepStageType(SleepSessionRecord.STAGE_TYPE_AWAKE))
        assertEquals(SleepStageType.Awake, sleepStageType(SleepSessionRecord.STAGE_TYPE_OUT_OF_BED))
        assertEquals(
            SleepStageType.AwakeInBed,
            sleepStageType(SleepSessionRecord.STAGE_TYPE_AWAKE_IN_BED),
        )
        assertEquals(SleepStageType.Light, sleepStageType(SleepSessionRecord.STAGE_TYPE_LIGHT))
        assertEquals(SleepStageType.Deep, sleepStageType(SleepSessionRecord.STAGE_TYPE_DEEP))
        assertEquals(SleepStageType.Rem, sleepStageType(SleepSessionRecord.STAGE_TYPE_REM))
        assertEquals(SleepStageType.Unknown, sleepStageType(-1))
    }

    @Test
    fun toPayload_mapsStepsCaloriesAndDistance() {
        val start = Instant.parse("2026-09-01T08:00:00Z")
        val end = Instant.parse("2026-09-01T09:00:00Z")

        val steps: Record = StepsRecord(
            startTime = start,
            startZoneOffset = ZoneOffset.UTC,
            endTime = end,
            endZoneOffset = ZoneOffset.UTC,
            count = 4_200,
            metadata = Metadata.manualEntryWithId("steps-1"),
        )
        assertEquals(
            HealthRecordPayload.Steps(
                count = 4_200,
                start = start,
                end = end,
                hcRecordId = "steps-1",
            ),
            steps.toPayload(),
        )

        val calories: Record = TotalCaloriesBurnedRecord(
            startTime = start,
            startZoneOffset = ZoneOffset.UTC,
            endTime = end,
            endZoneOffset = ZoneOffset.UTC,
            energy = Energy.kilocalories(180.0),
            metadata = Metadata.manualEntryWithId("kcal-1"),
        )
        assertEquals(
            HealthRecordPayload.Calories(
                kcal = 180.0,
                start = start,
                end = end,
                hcRecordId = "kcal-1",
            ),
            calories.toPayload(),
        )

        val distance: Record = DistanceRecord(
            startTime = start,
            startZoneOffset = ZoneOffset.UTC,
            endTime = end,
            endZoneOffset = ZoneOffset.UTC,
            distance = Length.meters(1_250.0),
            metadata = Metadata.manualEntryWithId("distance-1"),
        )
        assertEquals(
            HealthRecordPayload.Distance(
                meters = 1_250.0,
                start = start,
                end = end,
                hcRecordId = "distance-1",
            ),
            distance.toPayload(),
        )
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

class HealthConnectPermissionsTest {
    @Test
    fun hasEssentialAccess_requiresEveryRecordReadPermission() {
        val granted = HealthConnectPermissions.recordReadPermissions
        assertTrue(HealthConnectPermissions.hasEssentialAccess(granted))
        assertTrue(!HealthConnectPermissions.hasEssentialAccess(granted.drop(1).toSet()))
    }
}
