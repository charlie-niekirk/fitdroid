package com.fitdroid.core.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import net.openid.appauth.AuthState

@SingleIn(AppScope::class)
@Inject
@Suppress("DEPRECATION")
class EncryptedAuthStateStore(
    context: Context,
) {
    private val prefs: SharedPreferences

    init {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        prefs = EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    fun load(): AuthState? {
        val json = prefs.getString(KEY_AUTH_STATE, null) ?: return null
        return runCatching { AuthState.jsonDeserialize(json) }.getOrNull()
    }

    fun save(state: AuthState) {
        prefs.edit().putString(KEY_AUTH_STATE, state.jsonSerializeString()).apply()
    }

    fun clear() {
        prefs.edit().remove(KEY_AUTH_STATE).apply()
    }

    private companion object {
        const val PREFS_NAME = "fitdroid_auth_state"
        const val KEY_AUTH_STATE = "auth_state"
    }
}
