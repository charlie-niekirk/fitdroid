package com.fitdroid.core.database

import androidx.room.TypeConverter
import com.fitdroid.core.model.SleepStageType
import java.time.Instant
import java.time.LocalDate

class FitdroidConverters {
    @TypeConverter
    fun instantToEpochMilli(value: Instant?): Long? = value?.toEpochMilli()

    @TypeConverter
    fun epochMilliToInstant(value: Long?): Instant? = value?.let(Instant::ofEpochMilli)

    @TypeConverter
    fun localDateToString(value: LocalDate?): String? = value?.toString()

    @TypeConverter
    fun stringToLocalDate(value: String?): LocalDate? = value?.let(LocalDate::parse)

    @TypeConverter
    fun sleepStageTypeToString(value: SleepStageType?): String? = value?.name

    @TypeConverter
    fun stringToSleepStageType(value: String?): SleepStageType? =
        value?.let(SleepStageType::valueOf)
}
