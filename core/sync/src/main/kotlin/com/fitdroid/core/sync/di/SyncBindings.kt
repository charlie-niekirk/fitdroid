package com.fitdroid.core.sync.di

import android.content.Context
import androidx.work.WorkerParameters
import com.fitdroid.core.scoring.ScoringEngine
import com.fitdroid.core.sync.FitdroidSynchronizer
import com.fitdroid.core.sync.SyncWorker
import com.fitdroid.core.ui.di.MetroWorkerFactory
import com.fitdroid.core.ui.di.WorkerKey
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.IntoMap
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import java.time.ZoneId

@ContributesTo(AppScope::class)
@BindingContainer
object SyncBindings {
    @Provides
    @SingleIn(AppScope::class)
    fun scoringEngine(zoneId: ZoneId): ScoringEngine = ScoringEngine(zoneId = zoneId)

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
