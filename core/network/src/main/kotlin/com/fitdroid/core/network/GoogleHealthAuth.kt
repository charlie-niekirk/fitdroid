package com.fitdroid.core.network

import com.fitdroid.core.auth.AccessTokenProvider
import com.fitdroid.core.auth.NotAuthorizedException
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class GoogleHealthAuthInterceptor(
    private val tokenProvider: AccessTokenProvider,
    private val featureFlag: GoogleHealthFeatureFlag,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        if (!featureFlag.isEnabled()) {
            return chain.proceed(chain.request())
        }
        val token = runBlocking { tokenProvider.accessToken() }
        val request = chain.request().newBuilder()
            .header("Authorization", "Bearer $token")
            .build()
        return chain.proceed(request)
    }
}

class GoogleHealthAuthenticator(
    private val tokenProvider: AccessTokenProvider,
) : okhttp3.Authenticator {
    override fun authenticate(route: okhttp3.Route?, response: okhttp3.Response): okhttp3.Request? {
        if (responseCount(response) >= 2) return null
        val token = runCatching {
            runBlocking { tokenProvider.accessToken(forceRefresh = true) }
        }.getOrElse { error ->
            if (error is NotAuthorizedException) return null
            throw error
        }
        return response.request.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()
    }

    private fun responseCount(response: okhttp3.Response): Int {
        var current: okhttp3.Response? = response
        var count = 1
        while (current?.priorResponse != null) {
            count++
            current = current.priorResponse
        }
        return count
    }
}
