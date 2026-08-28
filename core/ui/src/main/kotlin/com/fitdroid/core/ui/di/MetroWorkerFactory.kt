package com.fitdroid.core.ui.di

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.Multibinds
import dev.zacsweers.metro.SingleIn
import kotlin.reflect.KClass

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
class MetroWorkerFactory(
    private val workerProviders: Map<KClass<out ListenableWorker>, WorkerInstanceFactory<*>>,
) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters,
    ): ListenableWorker? {
        val workerClass = runCatching { Class.forName(workerClassName).kotlin }.getOrNull() ?: return null
        return workerProviders[workerClass]?.create(workerParameters)
    }

    fun interface WorkerInstanceFactory<T : ListenableWorker> {
        fun create(params: WorkerParameters): T
    }
}

@ContributesTo(AppScope::class)
interface WorkerMultibinds {
    @Multibinds(allowEmpty = true)
    val workerProviders: Map<KClass<out ListenableWorker>, MetroWorkerFactory.WorkerInstanceFactory<*>>
}
