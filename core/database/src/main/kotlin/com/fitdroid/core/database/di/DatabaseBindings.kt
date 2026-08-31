package com.fitdroid.core.database.di

import android.content.Context
import androidx.room.Room
import com.fitdroid.core.database.FitdroidDatabase
import com.fitdroid.core.database.dao.DailyMetricsDao
import com.fitdroid.core.database.dao.ExerciseSessionDao
import com.fitdroid.core.database.dao.HeartRateSampleDao
import com.fitdroid.core.database.dao.ScoreDao
import com.fitdroid.core.database.dao.SleepSessionDao
import com.fitdroid.core.database.dao.SyncStateDao
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@ContributesTo(AppScope::class)
@BindingContainer
object DatabaseBindings {
    @Provides
    @SingleIn(AppScope::class)
    fun database(context: Context): FitdroidDatabase =
        Room.databaseBuilder(context, FitdroidDatabase::class.java, "fitdroid.db")
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    @Provides
    fun sleepSessionDao(database: FitdroidDatabase): SleepSessionDao = database.sleepSessionDao()

    @Provides
    fun dailyMetricsDao(database: FitdroidDatabase): DailyMetricsDao = database.dailyMetricsDao()

    @Provides
    fun exerciseSessionDao(database: FitdroidDatabase): ExerciseSessionDao =
        database.exerciseSessionDao()

    @Provides
    fun heartRateSampleDao(database: FitdroidDatabase): HeartRateSampleDao =
        database.heartRateSampleDao()

    @Provides
    fun scoreDao(database: FitdroidDatabase): ScoreDao = database.scoreDao()

    @Provides
    fun syncStateDao(database: FitdroidDatabase): SyncStateDao = database.syncStateDao()
}
