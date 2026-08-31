package com.fitdroid.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.fitdroid.core.database.entity.ExerciseSessionEntity
import kotlinx.coroutines.flow.Flow
import java.time.Instant

@Dao
interface ExerciseSessionDao {
    @Query("SELECT * FROM exercise_sessions ORDER BY start DESC")
    fun observeAll(): Flow<List<ExerciseSessionEntity>>

    @Query("SELECT * FROM exercise_sessions WHERE start >= :start AND start < :end ORDER BY start DESC")
    fun observeInRange(start: Instant, end: Instant): Flow<List<ExerciseSessionEntity>>

    @Query("SELECT * FROM exercise_sessions WHERE id = :id")
    suspend fun getById(id: String): ExerciseSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(session: ExerciseSessionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(sessions: List<ExerciseSessionEntity>)

    @Query("DELETE FROM exercise_sessions WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM exercise_sessions WHERE hcRecordId = :hcRecordId")
    suspend fun deleteByHcRecordId(hcRecordId: String)
}
