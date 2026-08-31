package com.fitdroid.core.model

import java.time.Instant

data class SyncState(
    val source: String,
    val lastSuccessAt: Instant? = null,
    val lastAttemptAt: Instant? = null,
    val lastError: String? = null,
) {
    companion object {
        const val SOURCE_HEALTH_CONNECT = "health_connect"
        const val SOURCE_GOOGLE_HEALTH = "google_health"
    }
}
