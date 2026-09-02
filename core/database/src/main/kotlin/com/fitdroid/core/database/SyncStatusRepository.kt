package com.fitdroid.core.database

import com.fitdroid.core.database.dao.SyncStateDao
import com.fitdroid.core.database.mapper.toModel
import com.fitdroid.core.model.SyncState
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface SyncStatusRepository {
    fun observe(): Flow<List<SyncState>>
}

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
class RoomSyncStatusRepository(
    private val dao: SyncStateDao,
) : SyncStatusRepository {
    override fun observe(): Flow<List<SyncState>> =
        dao.observeAll().map { rows -> rows.map { it.toModel() } }
}
