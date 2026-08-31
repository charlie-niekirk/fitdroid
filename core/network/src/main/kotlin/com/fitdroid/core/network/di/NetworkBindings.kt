package com.fitdroid.core.network.di

import com.fitdroid.core.auth.AccessTokenProvider
import com.fitdroid.core.network.GoogleHealthApi
import com.fitdroid.core.network.GoogleHealthAuthInterceptor
import com.fitdroid.core.network.GoogleHealthAuthenticator
import com.fitdroid.core.network.GoogleHealthFeatureFlag
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.create

@ContributesTo(AppScope::class)
@BindingContainer
object NetworkBindings {
    @Provides
    @SingleIn(AppScope::class)
    fun json(): Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        isLenient = true
    }

    @Provides
    @SingleIn(AppScope::class)
    fun okHttpClient(
        tokenProvider: AccessTokenProvider,
        featureFlag: GoogleHealthFeatureFlag,
    ): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(GoogleHealthAuthInterceptor(tokenProvider, featureFlag))
            .authenticator(GoogleHealthAuthenticator(tokenProvider))
            .build()

    @Provides
    @SingleIn(AppScope::class)
    fun googleHealthApi(client: OkHttpClient, json: Json): GoogleHealthApi {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl("https://health.googleapis.com/")
            .client(client)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create()
    }
}
