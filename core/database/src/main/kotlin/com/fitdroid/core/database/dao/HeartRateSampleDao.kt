package com.fitdroid.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.fitdroid.core.database.entity.HeartRateSampleEntity
import kotlinx.coroutines.flow.Flow
import java.time.Instant

@Dao
interface HeartRateSampleDao {
    @Query(
        "SELECT * FROM heart_rate_samples WHERE timestamp >= :start AND timestamp < :end ORDER BY timestamp ASC",
    )
    fun observeInRange(start: Instant, end: Instant): Flow<List<HeartRateSampleEntity>>

    @Query(
        "SELECT * FROM heart_rate_samples WHERE timestamp >= :start AND timestamp < :end ORDER BY timestamp ASC",
    )
    suspend fun getInRange(start: Instant, end: Instant): List<HeartRateSampleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(samples: List<HeartRateSampleEntity>)

    @Query("DELETE FROM heart_rate_samples WHERE timestamp >= :start AND timestamp < :end")
    suspend fun deleteInRange(start: Instant, end: Instant)

    @Query("DELETE FROM heart_rate_samples WHERE hcRecordId = :hcRecordId")
    suspend fun deleteByHcRecordId(hcRecordId: String)

    @Query("DELETE FROM heart_rate_samples WHERE sleepSessionId = :sleepSessionId")
    suspend fun deleteBySleepSessionId(sleepSessionId: String)
}
