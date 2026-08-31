package com.fitdroid.core.auth

object GoogleHealthScopes {
    const val SLEEP_READONLY =
        "https://www.googleapis.com/auth/googlehealth.sleep.readonly"
    const val HEALTH_METRICS_READONLY =
        "https://www.googleapis.com/auth/googlehealth.health_metrics_and_measurements.readonly"
    const val ACTIVITY_READONLY =
        "https://www.googleapis.com/auth/googlehealth.activity_and_fitness.readonly"

    val all: Set<String> = setOf(
        SLEEP_READONLY,
        HEALTH_METRICS_READONLY,
        ACTIVITY_READONLY,
    )
}
