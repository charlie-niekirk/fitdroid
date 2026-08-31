package com.fitdroid.core.database.dao

import com.fitdroid.core.database.entity.DailyMetricsEntity
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class DailyMetricsMergeTest {
    @Test
    fun merge_prefersIncomingNonNullFields() {
        val existing = DailyMetricsEntity(
            date = LocalDate.of(2026, 8, 28),
            steps = 4_000,
            restingHeartRateBpm = 58,
        )
        val incoming = DailyMetricsEntity(
            date = LocalDate.of(2026, 8, 28),
            hrvRmssdMs = 42.5,
            steps = 8_200,
        )

        val merged = existing.merge(incoming)

        assertEquals(58L, merged.restingHeartRateBpm)
        assertEquals(42.5, merged.hrvRmssdMs)
        assertEquals(8_200L, merged.steps)
    }
}
