package com.fitdroid.core.database

import com.fitdroid.core.model.SleepStageType
import java.time.Instant
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FitdroidConvertersTest {
    private val converters = FitdroidConverters()

    @Test
    fun instantRoundTrip() {
        val instant = Instant.parse("2026-08-28T14:48:00Z")
        assertEquals(instant, converters.epochMilliToInstant(converters.instantToEpochMilli(instant)))
        assertNull(converters.epochMilliToInstant(converters.instantToEpochMilli(null)))
    }

    @Test
    fun localDateRoundTrip() {
        val date = LocalDate.of(2026, 8, 28)
        assertEquals(date, converters.stringToLocalDate(converters.localDateToString(date)))
        assertNull(converters.stringToLocalDate(converters.localDateToString(null)))
    }

    @Test
    fun sleepStageTypeRoundTrip() {
        assertEquals(
            SleepStageType.Rem,
            converters.stringToSleepStageType(converters.sleepStageTypeToString(SleepStageType.Rem)),
        )
    }
}
