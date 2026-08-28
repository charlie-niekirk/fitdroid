package com.fitdroid.core.common.di

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import java.time.Clock
import java.time.ZoneId

@ContributesTo(AppScope::class)
@BindingContainer
object TimeBindings {
    @Provides
    @SingleIn(AppScope::class)
    fun clock(): Clock = Clock.systemDefaultZone()

    @Provides
    @SingleIn(AppScope::class)
    fun zoneId(): ZoneId = ZoneId.systemDefault()
}
