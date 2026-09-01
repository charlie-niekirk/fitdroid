package com.fitdroid.core.scoring

import java.time.Duration

data class ScoringGoals(
    val sleepTarget: Duration = Duration.ofHours(8),
    val steps: Long = 10_000,
    val activeMinutes: Int = 30,
    val cardioMinutes: Int = 22,
) {
    companion object {
        val Default: ScoringGoals = ScoringGoals()
    }
}
