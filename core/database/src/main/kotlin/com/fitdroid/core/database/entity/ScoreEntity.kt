package com.fitdroid.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "scores")
data class ScoreEntity(
    @PrimaryKey val date: LocalDate,
    val sleepScore: Int? = null,
    val sleepDuration: Int? = null,
    val sleepRestorative: Int? = null,
    val sleepEfficiency: Int? = null,
    val sleepDisturbances: Int? = null,
    val sleepConsistency: Int? = null,
    val readinessScore: Int? = null,
    val readinessHrv: Int? = null,
    val readinessRestingHeartRate: Int? = null,
    val readinessSleep: Int? = null,
    val readinessTrainingLoad: Int? = null,
    val readinessDegraded: Boolean = false,
    val activityScore: Int? = null,
    val activitySteps: Int? = null,
    val activityActiveMinutes: Int? = null,
    val activityCardioLoad: Int? = null,
)
