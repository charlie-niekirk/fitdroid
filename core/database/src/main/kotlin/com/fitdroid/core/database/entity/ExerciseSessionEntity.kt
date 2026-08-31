package com.fitdroid.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(
    tableName = "exercise_sessions",
    indices = [Index(value = ["hcRecordId"], unique = true)],
)
data class ExerciseSessionEntity(
    @PrimaryKey val id: String,
    val hcRecordId: String?,
    val start: Instant,
    val end: Instant,
    val activityType: String,
    val caloriesKcal: Double?,
)
