package com.fitdroid.core.network

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
class DefaultGoogleHealthFeatureFlag : GoogleHealthFeatureFlag {
    override fun isEnabled(): Boolean = false
}
