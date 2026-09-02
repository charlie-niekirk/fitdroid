package com.fitdroid.core.sync

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@SingleIn(AppScope::class)
@Inject
class SyncPolicy(
    private val scheduler: SyncScheduler,
    private val settings: UserSettingsRepository,
) {
    private val started = AtomicBoolean(false)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    fun start() {
        if (!started.compareAndSet(false, true)) return
        scope.launch {
            settings.settings
                .map { it.periodicSyncEnabled }
                .distinctUntilChanged()
                .collect { enabled ->
                    if (enabled) {
                        scheduler.schedulePeriodic()
                    } else {
                        scheduler.cancelPeriodic()
                    }
                }
        }
    }
}
