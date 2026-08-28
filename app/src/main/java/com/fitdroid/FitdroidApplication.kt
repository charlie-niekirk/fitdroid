package com.fitdroid

import android.app.Application
import androidx.work.Configuration
import com.fitdroid.di.AppGraph
import dev.zacsweers.metro.createGraphFactory

class FitdroidApplication : Application(), Configuration.Provider {
    val appGraph: AppGraph by lazy {
        createGraphFactory<AppGraph.Factory>().create(this)
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(appGraph.workerFactory)
            .build()
}
