package com.fitdroid.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.fitdroid.core.database.entity.ScoreEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface ScoreDao {
    @Query("SELECT * FROM scores WHERE date = :date")
    fun observe(date: LocalDate): Flow<ScoreEntity?>

    @Query("SELECT * FROM scores WHERE date >= :start AND date < :end ORDER BY date DESC")
    fun observeInRange(start: LocalDate, end: LocalDate): Flow<List<ScoreEntity>>

    @Query("SELECT * FROM scores WHERE date = :date")
    suspend fun get(date: LocalDate): ScoreEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ScoreEntity)

    @Query("DELETE FROM scores WHERE date = :date")
    suspend fun delete(date: LocalDate)
}
