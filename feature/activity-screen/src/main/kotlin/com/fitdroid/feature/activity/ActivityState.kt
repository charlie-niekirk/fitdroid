package com.fitdroid.feature.activity

import com.fitdroid.core.model.ActivityScore
import com.fitdroid.core.model.DailyMetrics
import com.fitdroid.core.model.ExerciseSession
import com.fitdroid.core.model.UserSettings
import java.time.LocalDate

data class ActivityState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val selectedDate: LocalDate = LocalDate.EPOCH,
    val today: LocalDate = LocalDate.EPOCH,
    val scoresByDate: Map<LocalDate, ActivityScore> = emptyMap(),
    val metricsByDate: Map<LocalDate, DailyMetrics> = emptyMap(),
    val exercisesByDate: Map<LocalDate, List<ExerciseSession>> = emptyMap(),
    val recentScores: List<Float> = emptyList(),
    val stepGoal: Long = UserSettings.DefaultSteps,
) {
    val score: ActivityScore? get() = scoresByDate[selectedDate]
    val metrics: DailyMetrics? get() = metricsByDate[selectedDate]
    val exercises: List<ExerciseSession> get() = exercisesByDate[selectedDate].orEmpty()
    val canGoPrevious: Boolean get() = selectedDate > today.minusDays(ScoreWindowDays - 1)
    val canGoNext: Boolean get() = selectedDate < today
    val hasDay: Boolean
        get() = score != null || metrics != null || exercises.isNotEmpty()
}

internal const val ScoreWindowDays = 30L
