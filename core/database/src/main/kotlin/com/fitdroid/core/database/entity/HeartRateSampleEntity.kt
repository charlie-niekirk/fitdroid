package com.fitdroid.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(
    tableName = "heart_rate_samples",
    indices = [
        Index("timestamp"),
        Index("hcRecordId"),
        Index("sleepSessionId"),
    ],
)
data class HeartRateSampleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Instant,
    val bpm: Long,
    val resolutionSeconds: Int = 1,
    val hcRecordId: String? = null,
    val sleepSessionId: String? = null,
)
