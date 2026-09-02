package com.fitdroid.feature.dashboard

import com.fitdroid.core.model.ActivityScore
import com.fitdroid.core.model.ReadinessScore
import com.fitdroid.core.model.SleepScore
import com.fitdroid.core.model.UserSettings
import java.time.LocalDate

data class DashboardState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val today: LocalDate = LocalDate.EPOCH,
    val sleepScore: SleepScore? = null,
    val readinessScore: ReadinessScore? = null,
    val activityScore: ActivityScore? = null,
    val sleepTrend: List<Float> = emptyList(),
    val steps: Long? = null,
    val stepGoal: Long = UserSettings.DefaultSteps,
) {
    val hasAnyScore: Boolean
        get() = sleepScore != null || readinessScore != null || activityScore != null
}

sealed interface DashboardEffect {
    data object OpenSleep : DashboardEffect
    data object OpenActivity : DashboardEffect
}

internal const val ScoreWindowDays = 30L
