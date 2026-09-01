package com.fitdroid.core.sync

import com.fitdroid.core.network.model.DailyHeartRateVariability
import com.fitdroid.core.network.model.DailyOxygenSaturation
import com.fitdroid.core.network.model.DailyRespiratoryRate
import com.fitdroid.core.network.model.DailyRestingHeartRate
import com.fitdroid.core.network.model.DailySleepTemperatureDerivations
import com.fitdroid.core.network.model.DataPoint
import com.fitdroid.core.network.model.GoogleDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GoogleHealthPointMappingTest {
    private val date = GoogleDate(2026, 8, 28)

    @Test
    fun toDailyMetrics_mapsEachMvpDailyType() {
        assertEquals(
            41.0,
            DataPoint(
                dailyHeartRateVariability = DailyHeartRateVariability(
                    date = date,
                    deepSleepRootMeanSquareOfSuccessiveDifferencesMilliseconds = 41.0,
                ),
            ).toDailyMetrics()?.hrvRmssdMs,
        )
        assertEquals(
            97.5,
            DataPoint(
                dailyOxygenSaturation = DailyOxygenSaturation(date = date, averagePercentage = 97.5),
            ).toDailyMetrics()?.spo2Percent,
        )
        assertEquals(
            14.2,
            DataPoint(
                dailyRespiratoryRate = DailyRespiratoryRate(date = date, breathsPerMinute = 14.2),
            ).toDailyMetrics()?.respiratoryRateBrpm,
        )
        assertEquals(
            54L,
            DataPoint(
                dailyRestingHeartRate = DailyRestingHeartRate(date = date, beatsPerMinute = 54),
            ).toDailyMetrics()?.restingHeartRateBpm,
        )
        assertEquals(
            1.0,
            DataPoint(
                dailySleepTemperatureDerivations = DailySleepTemperatureDerivations(
                    date = date,
                    nightlyTemperatureCelsius = 35.0,
                    baselineTemperatureCelsius = 34.0,
                ),
            ).toDailyMetrics()?.skinTempDeviationCelsius,
        )
        assertNull(DataPoint().toDailyMetrics())
    }
}
