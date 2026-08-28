package com.fitdroid.data

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

interface DataRepository {
    val data: Flow<List<String>>
}

@ContributesBinding(AppScope::class)
@Inject
class DefaultDataRepository : DataRepository {
    override val data: Flow<List<String>> = flowOf(listOf("Fitdroid"))
}
