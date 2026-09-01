package com.fitdroid.core.sync

import com.fitdroid.core.common.AppError
import com.fitdroid.core.common.result.Result
import com.fitdroid.core.health.HealthConnectChange
import com.fitdroid.core.health.HealthConnectDataSource
import com.fitdroid.core.health.HealthRecordPayload
import com.fitdroid.core.health.HeartRateDownsampler
import com.fitdroid.core.model.DailyMetrics
import com.fitdroid.core.model.HeartRateSample
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@SingleIn(AppScope::class)
@Inject
class HealthConnectSyncPass(
    private val dataSource: HealthConnectDataSource,
    private val store: LocalHealthStore,
    private val preferences: SyncPreferences,
    private val clock: Clock,
    private val zoneId: ZoneId,
) {
    suspend fun sync(): SyncPassResult =
        try {
            val token = preferences.healthConnectChangesToken()
            if (token == null) {
                initialSync()
            } else {
                deltaSync(token)
            }
            SyncPassResult.success()
        } catch (error: SyncPassException) {
            SyncPassResult.failed(error.error.message, retryable = error.retryable)
        }

    private suspend fun initialSync() {
        val token = unwrap(dataSource.newChangesToken())
        fullResync()
        val next = processChanges(token)
        preferences.saveHealthConnectChangesToken(next)
    }

    private suspend fun deltaSync(token: String) {
        val (next, expired) = processChangesAllowingExpiry(token)
        if (expired) {
            val fresh = unwrap(dataSource.newChangesToken())
            fullResync()
            val afterResync = processChanges(fresh)
            preferences.saveHealthConnectChangesToken(afterResync)
        } else {
            preferences.saveHealthConnectChangesToken(next)
        }
    }

    private suspend fun fullResync() {
        val (start, end) = syncWindow()
        val sleep = unwrap(dataSource.sleepSessions(start, end))
        val exercise = unwrap(dataSource.exerciseSessions(start, end))
        val heartRate = HeartRateDownsampler.downsample(
            samples = unwrap(dataSource.heartRate(start, end)),
            sleepSessions = sleep,
        )
        val paddedStart = start.minus(OverlapPadding)
        store.replaceSleep(paddedStart, end, sleep)
        store.replaceExercise(paddedStart, end, exercise)
        store.replaceHeartRate(paddedStart, end, heartRate)
        refreshDailyAggregates(datesInWindow(start, end))
    }

    private suspend fun processChanges(token: String): String {
        val (next, expired) = processChangesAllowingExpiry(token)
        if (expired) throw SyncPassException(AppError.Unknown("Changes token expired during processing"))
        return next
    }

    private suspend fun processChangesAllowingExpiry(token: String): Pair<String, Boolean> {
        var current = token
        var unknownDeletion = false
        val aggregateDates = mutableSetOf<LocalDate>()
        do {
            val page = unwrap(dataSource.changes(current))
            if (page.tokenExpired) {
                return current to true
            }
            for (change in page.changes) {
                when (change) {
                    is HealthConnectChange.Deletion -> {
                        if (!store.deleteByHcRecordId(change.recordId)) {
                            unknownDeletion = true
                        }
                    }

                    is HealthConnectChange.Upsert -> {
                        aggregateDates += applyUpsert(change.payload)
                    }
                }
            }
            current = page.nextToken
            if (!page.hasMore) break
        } while (true)
        if (unknownDeletion) {
            val (start, end) = syncWindow()
            refreshDailyAggregates(datesInWindow(start, end))
        } else if (aggregateDates.isNotEmpty()) {
            refreshDailyAggregates(aggregateDates)
        }
        return current to false
    }

    private suspend fun applyUpsert(payload: HealthRecordPayload): Set<LocalDate> =
        when (payload) {
            is HealthRecordPayload.Sleep -> {
                store.upsertSleep(payload.session)
                emptySet()
            }

            is HealthRecordPayload.Exercise -> {
                store.upsertExercise(payload.session)
                datesSpanned(payload.session.start, payload.session.end)
            }

            is HealthRecordPayload.HeartRate -> {
                upsertHeartRate(payload.samples)
                emptySet()
            }

            is HealthRecordPayload.RestingHeartRate -> {
                val date = payload.sample.time.atZone(zoneId).toLocalDate()
                store.mergeDailyMetrics(
                    DailyMetrics(date = date, restingHeartRateBpm = payload.sample.bpm),
                )
                emptySet()
            }

            is HealthRecordPayload.Steps -> datesSpanned(payload.start, payload.end)

            is HealthRecordPayload.Calories -> datesSpanned(payload.start, payload.end)

            is HealthRecordPayload.Distance -> datesSpanned(payload.start, payload.end)

            is HealthRecordPayload.Unknown -> emptySet()
        }

    private suspend fun upsertHeartRate(samples: List<HeartRateSample>) {
        if (samples.isEmpty()) return
        val minTime = samples.minOf { it.time }
        val maxTime = samples.maxOf { it.time }
        val sleep = store.sleepInRange(minTime.minus(OverlapPadding), maxTime.plus(OverlapPadding))
        store.upsertHeartRate(HeartRateDownsampler.downsample(samples, sleep))
    }

    private suspend fun refreshDailyAggregates(dates: Iterable<LocalDate>) {
        for (date in dates) {
            val dayStart = date.atStartOfDay(zoneId).toInstant()
            val dayEnd = date.plusDays(1).atStartOfDay(zoneId).toInstant()
            val steps = unwrap(dataSource.stepsTotal(dayStart, dayEnd))
            val calories = unwrap(dataSource.caloriesKcal(dayStart, dayEnd))
            val distance = unwrap(dataSource.distanceMeters(dayStart, dayEnd))
            val rhr = unwrap(dataSource.restingHeartRate(dayStart, dayEnd))
                .maxByOrNull { it.time }
                ?.bpm
            val exerciseMinutes = store.exerciseInRange(dayStart, dayEnd)
                .sumOf { it.duration.toMinutes().toInt().coerceAtLeast(0) }
            store.mergeDailyMetrics(
                DailyMetrics(
                    date = date,
                    restingHeartRateBpm = rhr,
                    steps = steps,
                    caloriesKcal = calories,
                    distanceMeters = distance,
                    exerciseMinutes = exerciseMinutes,
                ),
            )
        }
    }

    private fun syncWindow(): Pair<Instant, Instant> {
        val today = LocalDate.now(clock.withZone(zoneId))
        val start = today.minusDays(FullResyncDays - 1).atStartOfDay(zoneId).toInstant()
        val end = today.plusDays(1).atStartOfDay(zoneId).toInstant()
        return start to end
    }

    private fun datesInWindow(start: Instant, end: Instant): List<LocalDate> {
        val startDate = start.atZone(zoneId).toLocalDate()
        val endDate = end.atZone(zoneId).toLocalDate()
        return generateSequence(startDate) { it.plusDays(1) }
            .takeWhile { it.isBefore(endDate) }
            .toList()
    }

    private fun datesSpanned(start: Instant, end: Instant): Set<LocalDate> {
        val startDate = start.atZone(zoneId).toLocalDate()
        val endDate = if (end > start) end.minusMillis(1).atZone(zoneId).toLocalDate() else startDate
        return generateSequence(startDate) { it.plusDays(1) }
            .takeWhile { !it.isAfter(endDate) }
            .toSet()
    }

    private fun <T> unwrap(result: Result<T>): T =
        when (result) {
            is Result.Success -> result.data
            is Result.Failure -> throw SyncPassException(result.error)
            Result.Loading -> throw SyncPassException(AppError.Unknown("Unexpected loading state"))
        }

    companion object {
        const val FullResyncDays = 30L
        private val OverlapPadding: Duration = Duration.ofHours(16)
    }
}

internal class SyncPassException(
    val error: AppError,
) : Exception(error.message) {
    val retryable: Boolean
        get() = error !is AppError.PermissionDenied
}
