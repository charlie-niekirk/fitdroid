package com.fitdroid.core.sync.di

import android.content.Context
import androidx.work.WorkerParameters
import com.fitdroid.core.sync.FitdroidSynchronizer
import com.fitdroid.core.sync.ImmediateSync
import com.fitdroid.core.sync.ScoreRefresh
import com.fitdroid.core.sync.ScoreRefreshPass
import com.fitdroid.core.sync.SyncScheduler
import com.fitdroid.core.sync.SyncWorker
import com.fitdroid.core.ui.di.MetroWorkerFactory
import com.fitdroid.core.ui.di.WorkerKey
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.IntoMap
import dev.zacsweers.metro.Provides

@ContributesTo(AppScope::class)
@BindingContainer
object SyncBindings {
    @Provides
    fun immediateSync(scheduler: SyncScheduler): ImmediateSync = ImmediateSync(scheduler::requestImmediate)

    @Provides
    fun scoreRefresh(pass: ScoreRefreshPass): ScoreRefresh = ScoreRefresh(pass::refresh)

    @Provides
    @IntoMap
    @WorkerKey(SyncWorker::class)
    fun syncWorkerFactory(
        context: Context,
        synchronizer: FitdroidSynchronizer,
    ): MetroWorkerFactory.WorkerInstanceFactory<*> =
        MetroWorkerFactory.WorkerInstanceFactory { params: WorkerParameters ->
            SyncWorker(context, params, synchronizer)
        }
}
