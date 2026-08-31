package com.fitdroid.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.fitdroid.core.database.dao.DailyMetricsDao
import com.fitdroid.core.database.dao.ExerciseSessionDao
import com.fitdroid.core.database.dao.HeartRateSampleDao
import com.fitdroid.core.database.dao.ScoreDao
import com.fitdroid.core.database.dao.SleepSessionDao
import com.fitdroid.core.database.dao.SyncStateDao
import com.fitdroid.core.database.entity.DailyMetricsEntity
import com.fitdroid.core.database.entity.ExerciseSessionEntity
import com.fitdroid.core.database.entity.HeartRateSampleEntity
import com.fitdroid.core.database.entity.ScoreEntity
import com.fitdroid.core.database.entity.SleepSessionEntity
import com.fitdroid.core.database.entity.SleepStageEntity
import com.fitdroid.core.database.entity.SyncStateEntity

@Database(
    entities = [
        SleepSessionEntity::class,
        SleepStageEntity::class,
        DailyMetricsEntity::class,
        ExerciseSessionEntity::class,
        HeartRateSampleEntity::class,
        ScoreEntity::class,
        SyncStateEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(FitdroidConverters::class)
abstract class FitdroidDatabase : RoomDatabase() {
    abstract fun sleepSessionDao(): SleepSessionDao
    abstract fun dailyMetricsDao(): DailyMetricsDao
    abstract fun exerciseSessionDao(): ExerciseSessionDao
    abstract fun heartRateSampleDao(): HeartRateSampleDao
    abstract fun scoreDao(): ScoreDao
    abstract fun syncStateDao(): SyncStateDao
}
