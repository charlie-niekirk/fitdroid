package com.fitdroid.core.scoring

import kotlin.math.sqrt

internal object Statistics {
    fun mean(values: List<Double>): Double? {
        if (values.isEmpty()) return null
        return values.average()
    }

    fun sampleStdDev(values: List<Double>): Double? {
        if (values.size < 2) return null
        val average = values.average()
        val variance = values.sumOf { (it - average) * (it - average) } / (values.size - 1)
        return sqrt(variance)
    }

    fun zScore(value: Double, mean: Double, stdDev: Double): Double? {
        if (stdDev == 0.0) return null
        return (value - mean) / stdDev
    }
}

internal fun lerp(
    value: Double,
    from: Double,
    to: Double,
    fromScore: Double,
    toScore: Double,
): Double {
    if (to == from) return toScore
    val t = ((value - from) / (to - from)).coerceIn(0.0, 1.0)
    return fromScore + t * (toScore - fromScore)
}

internal fun Double.roundToScore(): Int = kotlin.math.round(this).toInt().coerceIn(0, 100)

internal fun weightedAverage(vararg parts: Pair<Int, Double>?): Int {
    val present = parts.filterNotNull()
    val totalWeight = present.sumOf { it.second }
    if (totalWeight <= 0.0) return 0
    return present.sumOf { it.first * it.second / totalWeight }.roundToScore()
}
