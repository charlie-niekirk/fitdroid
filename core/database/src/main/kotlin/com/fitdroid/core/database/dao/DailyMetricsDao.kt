package com.fitdroid.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.fitdroid.core.database.entity.DailyMetricsEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface DailyMetricsDao {
    @Query("SELECT * FROM daily_metrics WHERE date = :date")
    fun observe(date: LocalDate): Flow<DailyMetricsEntity?>

    @Query("SELECT * FROM daily_metrics WHERE date >= :start AND date < :end ORDER BY date DESC")
    fun observeInRange(start: LocalDate, end: LocalDate): Flow<List<DailyMetricsEntity>>

    @Query("SELECT * FROM daily_metrics WHERE date >= :start AND date < :end ORDER BY date DESC")
    suspend fun getInRange(start: LocalDate, end: LocalDate): List<DailyMetricsEntity>

    @Query("SELECT * FROM daily_metrics WHERE date = :date")
    suspend fun get(date: LocalDate): DailyMetricsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: DailyMetricsEntity)

    @Update
    suspend fun update(entity: DailyMetricsEntity)

    @Query("DELETE FROM daily_metrics WHERE date = :date")
    suspend fun delete(date: LocalDate)

    @Transaction
    suspend fun upsertMerging(incoming: DailyMetricsEntity) {
        val existing = get(incoming.date)
        if (existing == null) {
            insert(incoming)
        } else {
            update(existing.merge(incoming))
        }
    }
}

internal fun DailyMetricsEntity.merge(incoming: DailyMetricsEntity): DailyMetricsEntity =
    copy(
        restingHeartRateBpm = incoming.restingHeartRateBpm ?: restingHeartRateBpm,
        hrvRmssdMs = incoming.hrvRmssdMs ?: hrvRmssdMs,
        spo2Percent = incoming.spo2Percent ?: spo2Percent,
        respiratoryRateBrpm = incoming.respiratoryRateBrpm ?: respiratoryRateBrpm,
        skinTempDeviationCelsius = incoming.skinTempDeviationCelsius ?: skinTempDeviationCelsius,
        steps = incoming.steps ?: steps,
        caloriesKcal = incoming.caloriesKcal ?: caloriesKcal,
        distanceMeters = incoming.distanceMeters ?: distanceMeters,
        exerciseMinutes = incoming.exerciseMinutes ?: exerciseMinutes,
    )
