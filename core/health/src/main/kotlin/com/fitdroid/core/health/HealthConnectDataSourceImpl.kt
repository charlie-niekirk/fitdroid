package com.fitdroid.core.health

import android.os.RemoteException
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.changes.DeletionChange
import androidx.health.connect.client.changes.UpsertionChange
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.Record
import androidx.health.connect.client.records.RestingHeartRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.request.ChangesTokenRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.fitdroid.core.common.AppError
import com.fitdroid.core.common.result.Result
import com.fitdroid.core.model.ExerciseSession
import com.fitdroid.core.model.HeartRateSample
import com.fitdroid.core.model.RestingHeartRateSample
import com.fitdroid.core.model.SleepSession
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import java.time.Instant

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
class HealthConnectDataSourceImpl(
    private val clientProvider: HealthConnectClientProvider,
) : HealthConnectDataSource {
    override fun availability(): HealthConnectAvailability = clientProvider.availability()

    override fun requiredPermissions(): Set<String> {
        val client = clientProvider.clientOrNull() ?: return HealthConnectPermissions.recordReadPermissions
        return HealthConnectPermissions.allReadPermissions(client)
    }

    override suspend fun grantedPermissions(): Result<Set<String>> = runHealth {
        client().permissionController.getGrantedPermissions()
    }

    override suspend fun sleepSessions(start: Instant, end: Instant): Result<List<SleepSession>> =
        runHealth {
            readAll<SleepSessionRecord>(start, end).map { it.toSleepSession() }
        }

    override suspend fun heartRate(start: Instant, end: Instant): Result<List<HeartRateSample>> =
        runHealth {
            readAll<HeartRateRecord>(start, end).flatMap { it.toHeartRateSamples() }
        }

    override suspend fun restingHeartRate(
        start: Instant,
        end: Instant,
    ): Result<List<RestingHeartRateSample>> =
        runHealth {
            readAll<RestingHeartRateRecord>(start, end).map { it.toRestingHeartRateSample() }
        }

    override suspend fun stepsTotal(start: Instant, end: Instant): Result<Long> =
        runHealth {
            val response = client().aggregate(
                AggregateRequest(
                    metrics = setOf(StepsRecord.COUNT_TOTAL),
                    timeRangeFilter = TimeRangeFilter.between(start, end),
                ),
            )
            response[StepsRecord.COUNT_TOTAL] ?: 0L
        }

    override suspend fun exerciseSessions(
        start: Instant,
        end: Instant,
    ): Result<List<ExerciseSession>> =
        runHealth {
            readAll<ExerciseSessionRecord>(start, end).map { it.toExerciseSession() }
        }

    override suspend fun caloriesKcal(start: Instant, end: Instant): Result<Double> =
        runHealth {
            val response = client().aggregate(
                AggregateRequest(
                    metrics = setOf(TotalCaloriesBurnedRecord.ENERGY_TOTAL),
                    timeRangeFilter = TimeRangeFilter.between(start, end),
                ),
            )
            response[TotalCaloriesBurnedRecord.ENERGY_TOTAL]?.inKilocalories ?: 0.0
        }

    override suspend fun distanceMeters(start: Instant, end: Instant): Result<Double> =
        runHealth {
            val response = client().aggregate(
                AggregateRequest(
                    metrics = setOf(DistanceRecord.DISTANCE_TOTAL),
                    timeRangeFilter = TimeRangeFilter.between(start, end),
                ),
            )
            response[DistanceRecord.DISTANCE_TOTAL]?.inMeters ?: 0.0
        }

    override suspend fun newChangesToken(): Result<String> =
        runHealth {
            client().getChangesToken(
                ChangesTokenRequest(recordTypes = HealthConnectPermissions.recordTypes),
            )
        }

    override suspend fun changes(token: String): Result<HealthConnectChangesPage> =
        runHealth {
            val response = client().getChanges(token)
            HealthConnectChangesPage(
                changes = if (response.changesTokenExpired) {
                    emptyList()
                } else {
                    response.changes.mapNotNull { change ->
                        when (change) {
                            is DeletionChange -> HealthConnectChange.Deletion(change.recordId)
                            is UpsertionChange -> change.toUpsert()
                            else -> null
                        }
                    }
                },
                nextToken = response.nextChangesToken,
                hasMore = response.hasMore,
                tokenExpired = response.changesTokenExpired,
            )
        }

    private fun UpsertionChange.toUpsert(): HealthConnectChange.Upsert =
        HealthConnectChange.Upsert(
            recordId = record.metadata.id,
            lastModified = record.metadata.lastModifiedTime,
            payload = record.toPayload(),
        )

    private fun Record.toPayload(): HealthRecordPayload =
        when (this) {
            is SleepSessionRecord -> HealthRecordPayload.Sleep(toSleepSession())
            is HeartRateRecord -> HealthRecordPayload.HeartRate(toHeartRateSamples())
            is RestingHeartRateRecord -> HealthRecordPayload.RestingHeartRate(toRestingHeartRateSample())
            is StepsRecord -> toPayload()
            is ExerciseSessionRecord -> HealthRecordPayload.Exercise(toExerciseSession())
            is TotalCaloriesBurnedRecord -> toPayload()
            is DistanceRecord -> toPayload()
            else -> HealthRecordPayload.Unknown(
                type = this::class.simpleName.orEmpty(),
                hcRecordId = metadata.id,
            )
        }

    private suspend inline fun <reified T : Record> readAll(
        start: Instant,
        end: Instant,
    ): List<T> {
        val records = mutableListOf<T>()
        var pageToken: String? = null
        do {
            val response = client().readRecords(
                ReadRecordsRequest(
                    recordType = T::class,
                    timeRangeFilter = TimeRangeFilter.between(start, end),
                    pageToken = pageToken,
                ),
            )
            records += response.records
            pageToken = response.pageToken
        } while (pageToken != null)
        return records
    }

    private fun client(): HealthConnectClient =
        clientProvider.clientOrNull()
            ?: throw HealthConnectUnavailableException(clientProvider.availability())

    private suspend inline fun <T> runHealth(block: suspend () -> T): Result<T> =
        try {
            Result.Success(block())
        } catch (error: HealthConnectUnavailableException) {
            Result.Failure(
                AppError.Unavailable(
                    message = "Health Connect is not available (${error.availability})",
                    cause = error,
                ),
            )
        } catch (error: SecurityException) {
            Result.Failure(
                AppError.PermissionDenied(
                    message = error.message ?: "Health Connect permission denied",
                    cause = error,
                ),
            )
        } catch (error: RemoteException) {
            Result.Failure(
                AppError.Unavailable(
                    message = error.message ?: "Health Connect service is unavailable",
                    cause = error,
                ),
            )
        } catch (error: IllegalStateException) {
            Result.Failure(
                AppError.Unavailable(
                    message = error.message ?: "Health Connect is in an invalid state",
                    cause = error,
                ),
            )
        } catch (error: Exception) {
            Result.Failure(AppError.from(error))
        }
}

class HealthConnectUnavailableException(
    val availability: HealthConnectAvailability,
) : IllegalStateException("Health Connect status: $availability")
