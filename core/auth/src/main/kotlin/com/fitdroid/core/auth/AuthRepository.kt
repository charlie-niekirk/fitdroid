package com.fitdroid.core.auth

import android.content.Intent
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository : AccessTokenProvider {
    val isAuthorized: StateFlow<Boolean>

    fun authorizationIntent(): Intent

    suspend fun onAuthorizationResult(intent: Intent)

    suspend fun signOut()
}
