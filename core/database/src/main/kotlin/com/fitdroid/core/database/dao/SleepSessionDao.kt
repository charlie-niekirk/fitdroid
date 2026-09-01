package com.fitdroid.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.fitdroid.core.database.entity.SleepSessionEntity
import com.fitdroid.core.database.entity.SleepSessionWithStages
import com.fitdroid.core.database.entity.SleepStageEntity
import kotlinx.coroutines.flow.Flow
import java.time.Instant

@Dao
interface SleepSessionDao {
    @Transaction
    @Query("SELECT * FROM sleep_sessions ORDER BY start DESC")
    fun observeAll(): Flow<List<SleepSessionWithStages>>

    @Transaction
    @Query("SELECT * FROM sleep_sessions WHERE start >= :start AND start < :end ORDER BY start DESC")
    fun observeInRange(start: Instant, end: Instant): Flow<List<SleepSessionWithStages>>

    @Transaction
    @Query("SELECT * FROM sleep_sessions WHERE start >= :start AND start < :end ORDER BY start DESC")
    suspend fun getInRange(start: Instant, end: Instant): List<SleepSessionWithStages>

    @Transaction
    @Query("SELECT * FROM sleep_sessions WHERE id = :id")
    suspend fun getById(id: String): SleepSessionWithStages?

    @Query("SELECT * FROM sleep_sessions WHERE hcRecordId = :hcRecordId")
    suspend fun getByHcRecordId(hcRecordId: String): SleepSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SleepSessionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStages(stages: List<SleepStageEntity>)

    @Query("DELETE FROM sleep_stages WHERE sessionId = :sessionId")
    suspend fun deleteStagesForSession(sessionId: String)

    @Query("DELETE FROM sleep_sessions WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM sleep_sessions WHERE hcRecordId = :hcRecordId")
    suspend fun deleteByHcRecordId(hcRecordId: String): Int

    @Query("DELETE FROM sleep_sessions WHERE start >= :start AND start < :end")
    suspend fun deleteInRange(start: Instant, end: Instant)

    @Transaction
    suspend fun upsert(session: SleepSessionEntity, stages: List<SleepStageEntity>) {
        insertSession(session)
        deleteStagesForSession(session.id)
        if (stages.isNotEmpty()) {
            insertStages(stages)
        }
    }
}
