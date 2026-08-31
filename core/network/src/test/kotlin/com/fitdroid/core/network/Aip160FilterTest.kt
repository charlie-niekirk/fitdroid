package com.fitdroid.core.network

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

class Aip160FilterTest {
    @Test
    fun dateRange_usesDailyDateField() {
        val filter = Aip160Filter.dateRange(
            type = HealthDataType.DailyHeartRateVariability,
            startInclusive = LocalDate.of(2026, 8, 1),
            endExclusive = LocalDate.of(2026, 8, 2),
        )
        assertEquals(
            """daily-heart-rate-variability.date >= "2026-08-01" AND daily-heart-rate-variability.date < "2026-08-02"""",
            filter,
        )
    }

    @Test
    fun timeRange_encodesKindSpecificFields() {
        val start = Instant.parse("2026-08-28T00:00:00Z")
        val end = Instant.parse("2026-08-29T00:00:00Z")

        assertEquals(
            """respiratory-rate-sleep-summary.sample_time.physical_time >= "2026-08-28T00:00:00Z" AND respiratory-rate-sleep-summary.sample_time.physical_time < "2026-08-29T00:00:00Z"""",
            Aip160Filter.timeRange(HealthDataType.RespiratoryRateSleepSummary, start, end),
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun dateRange_rejectsNonDailyTypes() {
        Aip160Filter.dateRange(
            type = HealthDataType.RespiratoryRateSleepSummary,
            startInclusive = LocalDate.of(2026, 8, 1),
            endExclusive = LocalDate.of(2026, 8, 2),
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun timeRange_rejectsDailyTypes() {
        Aip160Filter.timeRange(
            type = HealthDataType.DailyRestingHeartRate,
            startInclusive = Instant.parse("2026-08-28T00:00:00Z"),
            endExclusive = Instant.parse("2026-08-29T00:00:00Z"),
        )
    }
}

class HealthDataTypeTest {
    @Test
    fun listUnsupportedTypes_areTheSixRollupOnlyKinds() {
        val unsupported = HealthDataType.entries.filterNot { it.supportsList }.map { it.path }.toSet()
        assertEquals(
            setOf(
                "floors",
                "total-calories",
                "active-minutes",
                "calories-in-heart-rate-zone",
                "time-in-heart-rate-zone",
                "daily-heart-rate-zones",
            ),
            unsupported,
        )
        assertTrue(HealthDataType.mvpPhysiological.all { it.supportsList })
        assertFalse(HealthDataType.Floors.supportsList)
    }
}
