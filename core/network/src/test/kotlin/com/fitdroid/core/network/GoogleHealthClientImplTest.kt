package com.fitdroid.core.network

import com.fitdroid.core.common.AppError
import com.fitdroid.core.common.result.Result
import com.fitdroid.core.network.model.DataPoint
import com.fitdroid.core.network.model.DataPointsResponse
import com.fitdroid.core.network.model.IdentityResponse
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class GoogleHealthClientImplTest {
    @Test
    fun verifyLinkage_whenFlagOff_returnsUnavailable() = runTest {
        val client = GoogleHealthClientImpl(
            api = FakeGoogleHealthApi(),
            featureFlag = GoogleHealthFeatureFlag { false },
        )

        val result = client.verifyLinkage()

        assertTrue(result is Result.Failure)
        assertTrue((result as Result.Failure).error is AppError.Unavailable)
    }

    @Test
    fun listAllDataPoints_whenListUnsupported_failsLoudly() = runTest {
        val client = GoogleHealthClientImpl(
            api = FakeGoogleHealthApi(),
            featureFlag = GoogleHealthFeatureFlag { true },
        )

        val result = client.listAllDataPoints(
            type = HealthDataType.Floors,
            startInclusive = LocalDate.of(2026, 8, 1),
            endExclusive = LocalDate.of(2026, 8, 2),
        )

        assertTrue(result is Result.Failure)
        assertTrue((result as Result.Failure).error.message.orEmpty().contains(":list"))
    }

    @Test
    fun listAllDataPoints_pagesUntilTokenIsEmpty() = runTest {
        val api = FakeGoogleHealthApi(
            pages = listOf(
                DataPointsResponse(dataPoints = listOf(DataPoint(name = "p1")), nextPageToken = "next"),
                DataPointsResponse(dataPoints = listOf(DataPoint(name = "p2")), nextPageToken = null),
            ),
        )
        val client = GoogleHealthClientImpl(
            api = api,
            featureFlag = GoogleHealthFeatureFlag { true },
        )

        val result = client.listAllDataPoints(
            type = HealthDataType.DailyHeartRateVariability,
            startInclusive = LocalDate.of(2026, 8, 1),
            endExclusive = LocalDate.of(2026, 8, 2),
        )

        val data = (result as Result.Success).data
        assertEquals(listOf("p1", "p2"), data.map { it.name })
        assertEquals(2, api.listCalls)
    }
}

private class FakeGoogleHealthApi(
    private val pages: List<DataPointsResponse> = emptyList(),
) : GoogleHealthApi {
    var listCalls: Int = 0
        private set

    override suspend fun getIdentity(): IdentityResponse =
        IdentityResponse(healthUserId = "user-1")

    override suspend fun listDataPoints(
        type: String,
        filter: String?,
        pageSize: Int,
        pageToken: String?,
    ): DataPointsResponse {
        val page = pages.getOrElse(listCalls) { DataPointsResponse() }
        listCalls++
        return page
    }
}
