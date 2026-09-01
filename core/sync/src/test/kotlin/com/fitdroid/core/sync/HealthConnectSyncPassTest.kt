package com.fitdroid.core.sync

import com.fitdroid.core.health.HealthConnectChange
import com.fitdroid.core.health.HealthConnectChangesPage
import com.fitdroid.core.health.HealthRecordPayload
import com.fitdroid.core.model.SleepSession
import com.fitdroid.core.model.SleepStage
import com.fitdroid.core.model.SleepStageType
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class HealthConnectSyncPassTest {
    private val zone = ZoneOffset.UTC
    private val clock = Clock.fixed(Instant.parse("2026-08-28T12:00:00Z"), zone)

    @Test
    fun initialSync_readsWindowThenPersistsChangesToken() = runTest {
        val dataSource = FakeHealthConnectDataSource().apply {
            nextToken = "fresh-token"
            sleep = listOf(sleepSession("sleep-1"))
            steps = 8_000
        }
        val store = FakeLocalHealthStore()
        val prefs = FakeSyncPreferences()
        val pass = HealthConnectSyncPass(dataSource, store, prefs, clock, zone)

        val result = pass.sync()

        assertTrue(result.isSuccess)
        assertEquals("fresh-token", prefs.healthConnectToken)
        assertEquals(1, store.sleep.size)
        assertEquals("sleep-1", store.sleep.first().id)
        assertTrue(store.metrics.isNotEmpty())
        assertEquals(8_000L, store.metrics.values.first().steps)
        assertEquals(1, dataSource.newTokenCalls)
    }

    @Test
    fun deltaSync_appliesUpsertsAndDeletions() = runTest {
        val store = FakeLocalHealthStore().apply {
            sleep += sleepSession("keep")
            sleep += sleepSession("gone")
        }
        val prefs = FakeSyncPreferences().apply { healthConnectToken = "delta-token" }
        val dataSource = FakeHealthConnectDataSource().apply {
            changesPages += HealthConnectChangesPage(
                changes = listOf(
                    HealthConnectChange.Upsert(
                        recordId = "keep",
                        lastModified = Instant.parse("2026-08-28T08:00:00Z"),
                        payload = HealthRecordPayload.Sleep(sleepSession("keep", notes = "updated")),
                    ),
                    HealthConnectChange.Deletion("gone"),
                ),
                nextToken = "next-token",
                hasMore = false,
                tokenExpired = false,
            )
        }
        val pass = HealthConnectSyncPass(dataSource, store, prefs, clock, zone)

        val result = pass.sync()

        assertTrue(result.isSuccess)
        assertEquals("next-token", prefs.healthConnectToken)
        assertEquals(listOf("keep"), store.sleep.map { it.id })
        assertEquals("updated", store.sleep.single().notes)
        assertEquals(0, dataSource.newTokenCalls)
    }

    @Test
    fun expiredToken_triggersBoundedFullResync() = runTest {
        val store = FakeLocalHealthStore().apply { sleep += sleepSession("stale") }
        val prefs = FakeSyncPreferences().apply { healthConnectToken = "expired" }
        val dataSource = FakeHealthConnectDataSource().apply {
            nextToken = "replacement"
            sleep = listOf(sleepSession("fresh"))
            changesPages += HealthConnectChangesPage(
                changes = emptyList(),
                nextToken = "expired",
                hasMore = false,
                tokenExpired = true,
            )
        }
        val pass = HealthConnectSyncPass(dataSource, store, prefs, clock, zone)

        val result = pass.sync()

        assertTrue(result.isSuccess)
        assertEquals("replacement", prefs.healthConnectToken)
        assertEquals(listOf("fresh"), store.sleep.map { it.id })
        assertTrue(dataSource.newTokenCalls >= 1)
        assertFalse(store.sleep.any { it.id == "stale" })
    }

    private fun sleepSession(id: String, notes: String? = null): SleepSession {
        val start = Instant.parse("2026-08-27T22:00:00Z")
        val end = Instant.parse("2026-08-28T06:00:00Z")
        return SleepSession(
            id = id,
            hcRecordId = id,
            start = start,
            end = end,
            stages = listOf(SleepStage(SleepStageType.Light, start, end)),
            notes = notes,
        )
    }
}
