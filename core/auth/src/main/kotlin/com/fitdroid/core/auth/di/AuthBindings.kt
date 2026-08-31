package com.fitdroid.core.auth.di

import android.content.Context
import com.fitdroid.core.auth.GoogleOAuthConfig
import com.fitdroid.core.auth.R
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@ContributesTo(AppScope::class)
@BindingContainer
object AuthBindings {
    @Provides
    @SingleIn(AppScope::class)
    fun googleOAuthConfig(context: Context): GoogleOAuthConfig =
        GoogleOAuthConfig(
            clientId = context.getString(R.string.core_auth_google_client_id),
            redirectUri = context.getString(R.string.core_auth_google_redirect_uri),
        )
}
