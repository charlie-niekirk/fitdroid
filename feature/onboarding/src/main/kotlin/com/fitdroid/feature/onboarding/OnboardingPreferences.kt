package com.fitdroid.feature.onboarding

import kotlinx.coroutines.flow.Flow

interface OnboardingPreferences {
    val isComplete: Flow<Boolean>

    suspend fun setComplete(complete: Boolean)
}
