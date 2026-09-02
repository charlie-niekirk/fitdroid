package com.fitdroid.core.sync

fun interface ScoreRefresh {
    suspend fun refresh()
}
