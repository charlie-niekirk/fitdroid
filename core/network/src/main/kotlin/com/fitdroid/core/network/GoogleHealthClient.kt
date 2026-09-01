package com.fitdroid.core.network

import com.fitdroid.core.common.AppError
import com.fitdroid.core.common.result.Result
import com.fitdroid.core.network.model.DataPoint
import com.fitdroid.core.network.model.IdentityResponse
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import java.time.Instant
import java.time.LocalDate
import retrofit2.HttpException

interface GoogleHealthClient {
    suspend fun verifyLinkage(): Result<IdentityResponse>

    suspend fun listAllDataPoints(
        type: HealthDataType,
        startInclusive: LocalDate,
        endExclusive: LocalDate,
    ): Result<List<DataPoint>>

    suspend fun listAllDataPoints(
        type: HealthDataType,
        startInclusive: Instant,
        endExclusive: Instant,
    ): Result<List<DataPoint>>
}

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
class GoogleHealthClientImpl(
    private val api: GoogleHealthApi,
    private val featureFlag: GoogleHealthFeatureFlag,
) : GoogleHealthClient {
    override suspend fun verifyLinkage(): Result<IdentityResponse> = runApi {
        ensureEnabled()
        val identity = api.getIdentity()
        if (identity.healthUserId.isNullOrBlank()) {
            throw IllegalStateException("Google Health identity did not include healthUserId")
        }
        identity
    }

    override suspend fun listAllDataPoints(
        type: HealthDataType,
        startInclusive: LocalDate,
        endExclusive: LocalDate,
    ): Result<List<DataPoint>> = runApi {
        ensureEnabled()
        ensureListSupported(type)
        pageAll(type, Aip160Filter.dateRange(type, startInclusive, endExclusive))
    }

    override suspend fun listAllDataPoints(
        type: HealthDataType,
        startInclusive: Instant,
        endExclusive: Instant,
    ): Result<List<DataPoint>> = runApi {
        ensureEnabled()
        ensureListSupported(type)
        pageAll(type, Aip160Filter.timeRange(type, startInclusive, endExclusive))
    }

    private suspend fun pageAll(type: HealthDataType, filter: String): List<DataPoint> {
        val points = mutableListOf<DataPoint>()
        var pageToken: String? = null
        do {
            val page = api.listDataPoints(
                type = type.path,
                filter = filter,
                pageSize = 1000,
                pageToken = pageToken,
            )
            points += page.dataPoints
            pageToken = page.nextPageToken
        } while (!pageToken.isNullOrEmpty())
        return points
    }

    private fun ensureEnabled() {
        if (!featureFlag.isEnabled()) {
            throw GoogleHealthDisabledException()
        }
    }

    private fun ensureListSupported(type: HealthDataType) {
        if (!type.supportsList) throw ListNotSupportedException(type)
    }

    private suspend inline fun <T> runApi(block: suspend () -> T): Result<T> =
        try {
            Result.Success(block())
        } catch (error: GoogleHealthDisabledException) {
            Result.Failure(
                AppError.Unavailable(
                    message = "Google Health API is disabled",
                    cause = error,
                ),
            )
        } catch (error: ListNotSupportedException) {
            Result.Failure(
                AppError.Unavailable(
                    message = error.message ?: "This data type does not support :list",
                    cause = error,
                ),
            )
        } catch (error: HttpException) {
            Result.Failure(
                AppError.Network(
                    message = error.message ?: "Google Health API request failed",
                    cause = error,
                ),
            )
        } catch (error: Exception) {
            Result.Failure(AppError.from(error))
        }
}

class GoogleHealthDisabledException : IllegalStateException("Google Health feature flag is off")
