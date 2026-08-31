package com.fitdroid.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "sync_state")
data class SyncStateEntity(
    @PrimaryKey val source: String,
    val lastSuccessAt: Instant? = null,
    val lastAttemptAt: Instant? = null,
    val lastError: String? = null,
)
