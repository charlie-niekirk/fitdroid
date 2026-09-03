package com.fitdroid.core.scoring

import com.fitdroid.core.model.UserSettings
import java.time.Duration

fun UserSettings.toScoringGoals(): ScoringGoals = ScoringGoals(
    sleepTarget = Duration.ofMinutes(sleepTargetMinutes.toLong()),
    steps = steps,
    activeMinutes = activeMinutes,
    cardioMinutes = cardioMinutes,
)
