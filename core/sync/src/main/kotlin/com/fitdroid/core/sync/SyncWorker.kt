package com.fitdroid.core.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class SyncWorker(
    appContext: Context,
    params: WorkerParameters,
    private val synchronizer: FitdroidSynchronizer,
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val outcome = synchronizer.sync()
        return if (outcome.retry) Result.retry() else Result.success()
    }

    companion object {
        const val UNIQUE_PERIODIC_WORK = "com.fitdroid.sync.periodic"
        const val UNIQUE_ONE_TIME_WORK = "com.fitdroid.sync.one_time"
    }
}
