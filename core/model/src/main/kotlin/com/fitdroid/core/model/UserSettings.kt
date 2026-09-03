package com.fitdroid.core.model

data class UserSettings(
    val sleepTargetMinutes: Int = DefaultSleepTargetMinutes,
    val steps: Long = DefaultSteps,
    val activeMinutes: Int = DefaultActiveMinutes,
    val cardioMinutes: Int = DefaultCardioMinutes,
    val periodicSyncEnabled: Boolean = true,
    val useClassicHypnogram: Boolean = false,
) {
    companion object {
        const val DefaultSleepTargetMinutes = 8 * 60
        const val DefaultSteps = 10_000L
        const val DefaultActiveMinutes = 30
        const val DefaultCardioMinutes = 22
        const val MinSleepTargetMinutes = 6 * 60
        const val MaxSleepTargetMinutes = 10 * 60
        const val SleepTargetStepMinutes = 15
        const val MinSteps = 2_000L
        const val MaxSteps = 20_000L
        const val StepsStep = 500L
        const val MinActiveMinutes = 10
        const val MaxActiveMinutes = 90
        const val ActiveMinutesStep = 5
        const val MinCardioMinutes = 10
        const val MaxCardioMinutes = 60
        const val CardioMinutesStep = 5

        val Default: UserSettings = UserSettings()
    }
}
