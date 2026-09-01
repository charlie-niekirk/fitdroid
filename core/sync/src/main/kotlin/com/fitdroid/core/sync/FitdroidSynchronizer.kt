package com.fitdroid.core.sync

import com.fitdroid.core.model.SyncState
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import java.time.Clock
import java.time.Instant

@SingleIn(AppScope::class)
@Inject
class FitdroidSynchronizer(
    private val healthConnectPass: HealthConnectSyncPass,
    private val googleHealthPass: GoogleHealthSyncPass,
    private val scoreRefreshPass: ScoreRefreshPass,
    private val store: LocalHealthStore,
    private val clock: Clock,
) {
    suspend fun sync(): SyncOutcome {
        val now = Instant.now(clock)
        val healthConnect = healthConnectPass.sync()
        store.recordSyncAttempt(
            source = SyncState.SOURCE_HEALTH_CONNECT,
            at = now,
            success = healthConnect.isSuccess,
            error = healthConnect.error.takeUnless { healthConnect.isSuccess },
        )
        val googleHealth = googleHealthPass.sync()
        store.recordSyncAttempt(
            source = SyncState.SOURCE_GOOGLE_HEALTH,
            at = now,
            success = googleHealth.isSuccess || googleHealth.isSkipped,
            error = googleHealth.error.takeUnless { googleHealth.isSuccess || googleHealth.isSkipped },
        )
        scoreRefreshPass.refresh()
        return SyncOutcome(healthConnect = healthConnect, googleHealth = googleHealth)
    }
}
