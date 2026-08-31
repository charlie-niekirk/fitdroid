package com.fitdroid.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(
    tableName = "sleep_sessions",
    indices = [Index(value = ["hcRecordId"], unique = true)],
)
data class SleepSessionEntity(
    @PrimaryKey val id: String,
    val hcRecordId: String?,
    val hcLastModified: Instant?,
    val start: Instant,
    val end: Instant,
    val notes: String?,
)
