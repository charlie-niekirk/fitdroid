package com.fitdroid.core.auth

interface AccessTokenProvider {
    suspend fun accessToken(forceRefresh: Boolean = false): String
}
