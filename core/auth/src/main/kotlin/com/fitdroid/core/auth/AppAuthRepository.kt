package com.fitdroid.core.auth

import android.content.Context
import android.content.Intent
import android.net.Uri
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.binding
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.openid.appauth.AuthState
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.ResponseTypeValues
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@ContributesBinding(AppScope::class, binding<AccessTokenProvider>())
@Inject
class AppAuthRepository(
    context: Context,
    private val config: GoogleOAuthConfig,
    private val store: EncryptedAuthStateStore,
) : AuthRepository {
    private val service = AuthorizationService(context.applicationContext)
    private val mutex = Mutex()
    private val serviceConfig = AuthorizationServiceConfiguration(
        Uri.parse(AUTHORIZATION_ENDPOINT),
        Uri.parse(TOKEN_ENDPOINT),
    )
    private val _isAuthorized = MutableStateFlow(store.load()?.isAuthorized == true)

    override val isAuthorized: StateFlow<Boolean> = _isAuthorized.asStateFlow()

    override fun authorizationIntent(): Intent {
        if (config.clientId.isBlank() || config.clientId == PLACEHOLDER_CLIENT_ID) {
            throw OAuthConfigException(
                "Google OAuth client ID is not configured. Set core_auth_google_client_id.",
            )
        }
        val request = AuthorizationRequest.Builder(
            serviceConfig,
            config.clientId,
            ResponseTypeValues.CODE,
            Uri.parse(config.redirectUri),
        )
            .setScopes(GoogleHealthScopes.all.toList())
            .setAdditionalParameters(
                mapOf(
                    "access_type" to "offline",
                ),
            )
            .build()
        return service.getAuthorizationRequestIntent(request)
    }

    override suspend fun onAuthorizationResult(intent: Intent) {
        mutex.withLock {
            val response = AuthorizationResponse.fromIntent(intent)
            val exception = AuthorizationException.fromIntent(intent)
            val state = store.load() ?: AuthState()
            state.update(response, exception)
            if (response == null) {
                store.save(state)
                _isAuthorized.value = false
                throw exception ?: NotAuthorizedException()
            }
            val tokenResponse = suspendCancellableCoroutine { continuation ->
                service.performTokenRequest(response.createTokenExchangeRequest()) { tokenResp, tokenEx ->
                    when {
                        tokenEx != null -> continuation.resumeWithException(tokenEx)
                        tokenResp != null -> continuation.resume(tokenResp)
                        else -> continuation.resumeWithException(NotAuthorizedException())
                    }
                }
            }
            state.update(tokenResponse, null)
            store.save(state)
            _isAuthorized.value = state.isAuthorized
        }
    }

    override suspend fun accessToken(forceRefresh: Boolean): String =
        mutex.withLock {
            val state = store.load() ?: throw NotAuthorizedException()
            if (!state.isAuthorized && state.refreshToken == null) {
                throw NotAuthorizedException()
            }
            if (forceRefresh) {
                state.setNeedsTokenRefresh(true)
            }
            val token = suspendCancellableCoroutine { continuation ->
                state.performActionWithFreshTokens(service) { accessToken, _, ex ->
                    when {
                        ex != null -> continuation.resumeWithException(ex)
                        accessToken != null -> continuation.resume(accessToken)
                        else -> continuation.resumeWithException(NotAuthorizedException())
                    }
                }
            }
            store.save(state)
            _isAuthorized.value = state.isAuthorized
            token
        }

    override suspend fun signOut() {
        mutex.withLock {
            store.clear()
            _isAuthorized.value = false
        }
    }

    private companion object {
        const val AUTHORIZATION_ENDPOINT = "https://accounts.google.com/o/oauth2/v2/auth"
        const val TOKEN_ENDPOINT = "https://oauth2.googleapis.com/token"
        const val PLACEHOLDER_CLIENT_ID = "YOUR_ANDROID_OAUTH_CLIENT_ID"
    }
}
