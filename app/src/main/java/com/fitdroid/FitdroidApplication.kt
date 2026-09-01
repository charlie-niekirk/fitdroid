package com.fitdroid

import android.app.Application
import androidx.work.Configuration
import com.fitdroid.di.AppGraph
import dev.zacsweers.metro.createGraphFactory
import dev.zacsweers.metrox.android.MetroAppComponentProviders
import dev.zacsweers.metrox.android.MetroApplication

class FitdroidApplication : Application(), MetroApplication, Configuration.Provider {
    val appGraph: AppGraph by lazy {
        createGraphFactory<AppGraph.Factory>().create(this)
    }

    override fun onCreate() {
        super.onCreate()
        appGraph.syncScheduler.schedulePeriodic()
    }

    override val appComponentProviders: MetroAppComponentProviders
        get() = appGraph

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(appGraph.workerFactory)
            .build()
}
