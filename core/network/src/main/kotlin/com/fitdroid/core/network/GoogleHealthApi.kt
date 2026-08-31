package com.fitdroid.core.network

import com.fitdroid.core.network.model.DataPointsResponse
import com.fitdroid.core.network.model.IdentityResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface GoogleHealthApi {
    @GET("v4/users/me/identity")
    suspend fun getIdentity(): IdentityResponse

    @GET("v4/users/me/dataTypes/{type}/dataPoints")
    suspend fun listDataPoints(
        @Path("type") type: String,
        @Query("filter") filter: String? = null,
        @Query("pageSize") pageSize: Int = 1000,
        @Query("pageToken") pageToken: String? = null,
    ): DataPointsResponse
}
