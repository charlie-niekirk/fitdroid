package com.fitdroid.di

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import androidx.work.WorkerFactory
import com.fitdroid.core.sync.SyncPolicy
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metrox.android.MetroAppComponentProviders
import dev.zacsweers.metrox.viewmodel.ViewModelGraph

@DependencyGraph(AppScope::class)
interface AppGraph : MetroAppComponentProviders, ViewModelGraph {
    val application: Application
    val viewModelFactory: ViewModelProvider.Factory
    val workerFactory: WorkerFactory
    val syncPolicy: SyncPolicy

    @DependencyGraph.Factory
    fun interface Factory {
        fun create(@Provides application: Application): AppGraph
    }
}
