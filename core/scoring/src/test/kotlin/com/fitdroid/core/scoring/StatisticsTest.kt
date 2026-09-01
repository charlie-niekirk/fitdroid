package com.fitdroid.core.scoring

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class StatisticsTest {
    @Test
    fun mean_returnsNullForEmpty() {
        assertNull(Statistics.mean(emptyList()))
        assertEquals(5.0, Statistics.mean(listOf(2.0, 4.0, 9.0)))
    }

    @Test
    fun sampleStdDev_requiresTwoValues() {
        assertNull(Statistics.sampleStdDev(listOf(3.0)))
        assertEquals(1.0, Statistics.sampleStdDev(listOf(1.0, 2.0, 3.0))!!, 0.0001)
    }

    @Test
    fun zScore_isNullWhenStdDevIsZero() {
        assertNull(Statistics.zScore(5.0, 5.0, 0.0))
        assertEquals(1.0, Statistics.zScore(12.0, 10.0, 2.0)!!, 0.0001)
    }

    @Test
    fun weightedAverage_renormalizesMissingParts() {
        assertEquals(0, weightedAverage())
        assertEquals(100, weightedAverage(100 to 1.0))
        assertEquals(50, weightedAverage(100 to 0.5, 0 to 0.5))
        assertEquals(100, weightedAverage(100 to 0.5, null))
    }
}
