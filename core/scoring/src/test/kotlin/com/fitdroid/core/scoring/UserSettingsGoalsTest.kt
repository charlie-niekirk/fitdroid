package com.fitdroid.core.scoring

import com.fitdroid.core.model.UserSettings
import java.time.Duration
import org.junit.Assert.assertEquals
import org.junit.Test

class UserSettingsGoalsTest {
    @Test
    fun toScoringGoals_mapsMinutesAndActivityTargets() {
        val goals = UserSettings(
            sleepTargetMinutes = 7 * 60,
            steps = 8_000,
            activeMinutes = 45,
            cardioMinutes = 20,
        ).toScoringGoals()
        assertEquals(Duration.ofHours(7), goals.sleepTarget)
        assertEquals(8_000L, goals.steps)
        assertEquals(45, goals.activeMinutes)
        assertEquals(20, goals.cardioMinutes)
    }
}
